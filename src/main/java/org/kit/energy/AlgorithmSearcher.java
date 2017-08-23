package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

    @Autowired
    private Environment environment;

    public Map<ForecastAlgorithm, Class<?>> beginSearch(String path){

        path = "C:/Users/qa5147/Documents/Klassen/"; //path = "org.kit.energy";

        /*
        // test
        List<Path> myList;
        List<Class<? extends AlgoPlugin>> myClassList = new ArrayList<>();

        // go through each jar file and put the algoPlugin-class to a set
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            myList = paths
                    .filter(f -> f.toString().endsWith(".jar"))
                    .collect(Collectors.toList());


            for(Path p : myList){
                Class<? extends AlgoPlugin> currentClass = readJarFile(p.toString());
                if(currentClass != null){
                    myClassList.add(currentClass);
                }
            }
        }
        catch (IOException e){
            System.out.println(e.toString());
        }
        System.out.println("loaded class list with plugins");
        // test

        // test
        Set<Class<? extends AlgoPlugin>> subtypes = new HashSet<>(myClassList);
        // test

        URL[] urls = new URL[0];
        try {
            urls = new URL[]{ new URL("file://C:/Users/qa5147/Documents/Klassen/") };
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        File myFile = new File("C:/Users/qa5147/Documents/Klassen/testTemplate-1.0-SNAPSHOT.jar");
        URLClassLoader cl = null;
        try {
            cl = new URLClassLoader(new URL[] {myFile.toURI().toURL()}, System.class.getClassLoader());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        */

        /*
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forClassLoader(cl)
                ).addUrls(urls).addClassLoader(cl).addScanners(new SubTypesScanner(),new FieldAnnotationsScanner()));
        */

        /*String pathToJar = "C:/Users/qa5147/Documents/Klassen/testJavaTemplate1.0.jar";

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(pathToJar);
        } catch (IOException io) {
            io.printStackTrace();
        }
        Enumeration<JarEntry> enumJars = jarFile.entries();
        URL url = null;
        try {
            url = new URL("jar:file:" + pathToJar+"!/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URL[] urls = { url };
        //URLClassLoader cl = URLClassLoader.newInstance(urls);
        URLClassLoader urlClassLoader = new URLClassLoader(urls,System.class.getClassLoader());
        Class<? extends AlgoPlugin> c = null;
        while(enumJars.hasMoreElements()){
            JarEntry je = enumJars.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class")){
                continue;
            }
            // -6 because of .class
            String className = je.getName().substring(0,je.getName().length()-6);
            System.out.println("current class name: "+className);
            className = className.replace('/', '.');
            try {
                if(className.equals("JavaAlgorithmExample")){
                    c = (Class<? extends AlgoPlugin>)urlClassLoader.loadClass(className);
                    System.out.println("uii: "+c.getName());
                }
            } catch (ClassNotFoundException ec) {
                ec.printStackTrace();
            }
        }*/

        /*Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forClassLoader(urlClassLoader)
                ).addClassLoader(urlClassLoader).addScanners(new FieldAnnotationsScanner(),new SubTypesScanner())
        );
        */


        Map<ForecastAlgorithm, Class<?>> forecastAlgorithmsWithPlugins = new HashedMap();

        //Reflections reflections = new Reflections("",new FieldAnnotationsScanner(), new SubTypesScanner(), urlClassLoader);

        Reflections reflections = new Reflections("",new FieldAnnotationsScanner(), new SubTypesScanner());

        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        //subtypes.add(c);

        for( Class<? extends AlgoPlugin> plugin : subtypes){

            // initialize values
            ArrayList<AlgoParam> paraList = new ArrayList<>();
            ArrayList<AlgoParameter> parameterList = new ArrayList<>();
            ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();

            // register plugin in factory
            forecastAlgorithm.setAlgoName(plugin.getSimpleName());

            // get fields with the annotaion AlgoParam
            Set<Field> fields = getAllFields(plugin, withAnnotation(AlgoParam.class));
            if(plugin.getSimpleName().equals("AlgorithmExample")){
                System.out.println("field length: "+fields.size());
            }

            if(!fields.isEmpty()){

                for(Field f:fields){
                    // get current Annotation
                    AlgoParam algoParam = f.getAnnotation(AlgoParam.class);

                    // create and set Parameter object with values from the annotation
                    AlgoParameter algoParameter = new AlgoParameter();

                    //Annotation paremeterAnnotation = getAnnotationWithName(f,"AlgoParam");

                    /*
                    // extract name and value from the annotation
                    String parameterString = paremeterAnnotation.toString();

                    int firstIndex = parameterString.indexOf("name=")+5;
                    int secondIndex = parameterString.indexOf(", value",firstIndex);
                    String name = parameterString.substring(firstIndex,secondIndex);

                    firstIndex = parameterString.indexOf("value=")+6;
                    secondIndex = parameterString.indexOf(")",firstIndex);
                    String value = parameterString.substring(firstIndex,secondIndex);
                    */

                    algoParameter.setName(algoParam.name().toString());
                    algoParameter.setValue(algoParam.value().toString());

                    //algoParameter.setName(name);
                    //algoParameter.setValue(value);

                    // add the paras to their lists
                    parameterList.add(algoParameter);

                    //paraList.add(algoParam);
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
        URLClassLoader cl = URLClassLoader.newInstance(urls,ClassLoader.getSystemClassLoader());

        try {
            JarInputStream myJarFile = new JarInputStream(new FileInputStream(path));
            JarEntry myJar;
            while (true) {
                myJar = myJarFile.getNextJarEntry();
                if (myJar == null) {break;}
                if ((myJar.getName().endsWith(".class"))) {
                    String className = myJar.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));
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

    private ArrayList<Field> getFieldsWithAnnotation(Class<? extends AlgoPlugin> plugin, String annotationName){
        Field[] myFields = plugin.getDeclaredFields();
        ArrayList<Field> cleandedFields = new ArrayList<>();
        boolean annotationInside = false;
        for(Field fi: myFields){
            Annotation[] currentAnnos = fi.getAnnotations();
            for(Annotation currentAnno : currentAnnos){
                annotationInside = (currentAnno.annotationType().getSimpleName().equals(annotationName));
            }
            if(annotationInside){cleandedFields.add(fi);}
        }
        return cleandedFields;
    }

    private Annotation getAnnotationWithName(Field f, String annoName){
        Annotation[] currentAnnos = f.getAnnotations();
        boolean annotationInside = false;
        for(Annotation currentAnno : currentAnnos){
            annotationInside = currentAnno.annotationType().getSimpleName().equals(annoName);
            if(annotationInside){return currentAnno;}
        }
        return null;
    }
}
