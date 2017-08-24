package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.kit.energy.api.AlgoParam;
import org.kit.energy.api.AlgoPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Created by qa5147 on 28.06.2017.
 */
@Component
public class AlgorithmSearcher {

    @Autowired
    private Environment environment;

    public Map<ForecastAlgorithm, Class<?>> beginSearch(String path){

        String pathToJar = "C:/Users/qa5147/Documents/Klassen/testFinal.jar";
        String templatePackageStructure = "bla.test";

        URL url = null;
        try {
            url = new URL("jar:file:" + pathToJar+"!/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URL[] urls = { url };

        URLClassLoader urlClassLoader = new URLClassLoader(urls,ForecastFrameworkApplication.class.getClassLoader());


        /*
        Class<? extends AlgoPlugin> myClass = null;
        try {
            myClass = (Class<? extends AlgoPlugin>) urlClassLoader.loadClass("ExampleAlgo");
        } catch (ClassNotFoundException e) {
            System.out.println("Fehler: "+e.toString());
        }*/


        Map<ForecastAlgorithm, Class<?>> forecastAlgorithmsWithPlugins = new HashedMap();

        //templatePackageStructure
        Reflections reflections = new Reflections(templatePackageStructure,urlClassLoader,new FieldAnnotationsScanner(), new SubTypesScanner());

        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        //subtypes.add(myClass);

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

                }
                forecastAlgorithm.setAlgoParameters(parameterList);
                forecastAlgorithmsWithPlugins.put(forecastAlgorithm,plugin);
            }

        }
        return forecastAlgorithmsWithPlugins;
    }

    private Class<? extends AlgoPlugin> readJarFile(String path) throws MalformedURLException {

        Class<? extends AlgoPlugin> classToReturn = null;
        URL[] urls = { new URL("jar:file:" + path+"!/") };
        URLClassLoader cl = URLClassLoader.newInstance(urls,ForecastFrameworkApplication.class.getClassLoader());

        try {
            JarInputStream myJarFile = new JarInputStream(new FileInputStream(path));
            JarEntry myJar;
            while (true) {
                myJar = myJarFile.getNextJarEntry();
                if (myJar == null) {break;}
                if ((myJar.getName().endsWith(".class"))) {
                    String className = myJar.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));
                    System.out.println("Now the class: "+myClass);
                    Class c = cl.loadClass(myClass);
                    boolean containsAlgoPluginInterface = false;
                    if(c.getInterfaces().length != 0){
                        for(Class<?> currentClass:c.getInterfaces()){
                            containsAlgoPluginInterface = (currentClass.getSimpleName().equals("AlgoPlugin"));
                        }
                    }
                    if(containsAlgoPluginInterface){
                        System.out.println("class to return was found: "+c.getSimpleName());
                        return c;
                    } else{
                        System.out.println("plugin interface not found for class "+c.getSimpleName());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Oops.. Encounter an issue while parsing jar: " + e.toString());
        }
        return classToReturn;
    }

}
