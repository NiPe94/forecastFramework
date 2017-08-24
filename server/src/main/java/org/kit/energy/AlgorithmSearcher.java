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

        String pathToJar = "C:/Users/qa5147/IdeaProjects/forecastFramework/testtemplate/target/test-template-0.0.1-SNAPSHOT.jar";

        URL url = null;
        try {
            url = new URL("jar:file:" + pathToJar+"!/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URL[] urls = { url };

        URLClassLoader urlClassLoader = new URLClassLoader(urls,ForecastFrameworkApplication.class.getClassLoader());


        Class<? extends AlgoPlugin> myClass = null;
        try {
            myClass = (Class<? extends AlgoPlugin>) urlClassLoader.loadClass("bla.test.TestTemplate");
            System.out.println(myClass.getSimpleName());
            Class myInterfaces = myClass.getInterfaces()[0];
            // true: boolean assignable = myInterfaces.isAssignableFrom(myClass);
            // true: boolean assignable = AlgoPlugin.class.isAssignableFrom(myClass);
            boolean assignable = AlgoPlugin.class.isAssignableFrom(myClass);
            System.out.println("Interface assignable from the testTemplate?: "+assignable);
        } catch (ClassNotFoundException e) {
            System.out.println("Fehler: "+e.toString());
        }

        Map<ForecastAlgorithm, Class<?>> forecastAlgorithmsWithPlugins = new HashedMap();

        Reflections reflections = new Reflections("",new FieldAnnotationsScanner(), new SubTypesScanner(),urlClassLoader);

        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        subtypes.add(myClass);

        System.out.println("length of subtypeList: "+subtypes.size());

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
