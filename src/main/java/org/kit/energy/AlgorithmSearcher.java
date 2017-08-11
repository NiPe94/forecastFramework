package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withTypeAssignableTo;

/**
 * Created by qa5147 on 28.06.2017.
 */
@Component
public class AlgorithmSearcher {

    public Map<ForecastAlgorithm, Class<?>> beginSearch(String path){

        path = "C:/Users/qa5147/Documents/Klassen/"; //path = "org.kit.energy";

        // test
        List<Path> myList;


                // go through each jar file and put the algoPlugin-class to a set
        Set<Class<? extends AlgoPlugin>> newSubtypes = null;

        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            myList = paths
                    .filter(f -> f.toString().endsWith(".jar"))
                    .collect(Collectors.toList());


            for(Path p : myList){
                Class<? extends AlgoPlugin> currentClass = readJarFile(p.toString());
                newSubtypes.add(currentClass);
            }
            System.out.println(newSubtypes.toString());
        }
        catch (IOException e){
            System.out.println(e.toString());
        }
        // test

        Map<ForecastAlgorithm, Class<?>> forecastAlgorithmsWithPlugins = new HashedMap();

        Reflections reflections = new Reflections(path, new FieldAnnotationsScanner(), new SubTypesScanner());
        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        System.out.println("sind da klassen: "+subtypes.size());

        for( Class<? extends AlgoPlugin> plugin : subtypes){

            // initialize values
            ArrayList<AlgoParam> paraList = new ArrayList<>();
            ArrayList<AlgoParameter> parameterList = new ArrayList<>();
            ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();

            // register plugin in factory
            forecastAlgorithm.setAlgoName(plugin.getSimpleName());

            // get fields with the annotaion AlgoParam
            Set<Field> fields = getAllFields(plugin, withAnnotation(AlgoParam.class));

            if(!fields.isEmpty()){

                for(Field f:fields){
                    // get current Annotation
                    AlgoParam algoParam = f.getAnnotation(AlgoParam.class);

                    // create and set Parameter object with values from the annotation
                    AlgoParameter algoParameter = new AlgoParameter();
                    algoParameter.setName(algoParam.name().toString());
                    algoParameter.setValue(algoParam.value().toString());

                    // add the paras to their lists
                    parameterList.add(algoParameter);
                    paraList.add(algoParam);
                }
                forecastAlgorithm.setAlgoParameters(parameterList);
                forecastAlgorithmsWithPlugins.put(forecastAlgorithm,plugin);
            }

        }
        return forecastAlgorithmsWithPlugins;
    }

    private Class<? extends AlgoPlugin> readJarFile(String path) throws MalformedURLException {

        URL[] urls = { new URL("jar:file:" + path+"!/") };
        URLClassLoader cl = URLClassLoader.newInstance(urls);

        JSONArray listofClasses = new JSONArray();
        JSONObject myObject = new JSONObject();

        try {
            JarInputStream myJarFile = new JarInputStream(new FileInputStream(path));
            JarEntry myJar;
            while (true) {
                myJar = myJarFile.getNextJarEntry();
                if (myJar == null) {
                    break;
                }
                if ((myJar.getName().endsWith(".class"))) {
                    String className = myJar.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));
                    Class c = cl.loadClass(myClass);

                    System.out.println("class name: "+c.getSimpleName());

                    System.out.println("assignable: "+(AlgoPlugin.class.isAssignableFrom(c)));

                    if(c.getInterfaces().length != 0){
                        System.out.println("inside");
                        System.out.println(c.getInterfaces()[0].toString());
                        System.out.println("wirklich nicht?: "+(AlgoPlugin.class.isAssignableFrom(c)));
                    }


                    //withTypeAssignableTo(AlgoPlugin.class);
                    listofClasses.put(myClass);
                }
            }
            myObject.put("Jar File Name", path);
            myObject.put("List of Class", listofClasses);
        } catch (Exception e) {
            System.out.println("Oops.. Encounter an issue while parsing jar" + e.toString());
        } finally {

        }

        System.out.println("here it is:");
        System.out.println(listofClasses.toString());

        return null;
    }
}
