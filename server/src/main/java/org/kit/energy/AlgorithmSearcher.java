package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.kit.energy.api.AlgoParam;
import org.kit.energy.api.AlgoPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Class to get algorithms via reflection from a directory
 */
@Component
public class AlgorithmSearcher {

    /**
     * String with a path to a directory in which forecast algorithms as jar files are located.
     */
    private final String finalPath;

    /**
     * Sets the directory path with a variable loaded from the application.properties file from the resources folder.
     * @param path the directory path loaded from application.properties.
     */
    public AlgorithmSearcher(@Value("${plugin.loading.path}") String path){
        this.finalPath = path;
    }

    /**
     * gets a mapping between algorithms names and algorithm classes via reflecion api with the help of a path from application.properties.
     * First the algorithms from jar files will get parsed, then they will be added to a set which will be processed to produce algorithm objects for the web ui.
     * @return a mapping between algorithm names and algorithm classes.
     * @throws MalformedURLException If the provided url to the directory with jar files is not valid.
     */
    public Map<ForecastAlgorithm, Class<?>> beginSearch(String path){

        String templatePackageStructure = "org.kit.energy";

        String pathToJars = this.finalPath;

        // go through each jar in the specific directory and load the plugins from there
        ArrayList<Class<? extends AlgoPlugin>> loadedPlugins = new ArrayList<>();
        File[] files = new File(pathToJars).listFiles();
        for(File file : files){
            if(file.isFile() && file.getName().endsWith(".jar")){
                Class<? extends AlgoPlugin> currentClass = null;
                try {
                    currentClass = readJarFile((pathToJars+file.getName()));
                    loadedPlugins.add(currentClass);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        Map<ForecastAlgorithm, Class<?>> forecastAlgorithmsWithPlugins = new HashedMap();

        Reflections reflections = new Reflections(templatePackageStructure,ForecastFrameworkApplication.class.getClassLoader(),new FieldAnnotationsScanner(), new SubTypesScanner());

        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        subtypes.addAll(loadedPlugins);

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
            }
            forecastAlgorithm.setAlgoParameters(parameterList);
            forecastAlgorithmsWithPlugins.put(forecastAlgorithm,plugin);

        }
        return forecastAlgorithmsWithPlugins;
    }

    /**
     * Gets a class from a jar file which implements the AlgoPlugin interface.
     * For that, a url to an jar entry is required.
     * @param path the path to a specific jar inside the directory defined in application.properties.
     * @return a class which implements the AlgoPlugin interface.
     * @see AlgoPlugin
     */
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
                    Class c = cl.loadClass(myClass);
                    boolean containsAlgoPluginInterface = false;
                    if(c.getInterfaces().length != 0){
                        for(Class<?> currentClass:c.getInterfaces()){
                            containsAlgoPluginInterface = (currentClass.getSimpleName().equals("AlgoPlugin"));
                        }
                    }
                    if(containsAlgoPluginInterface){
                        return c;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Oops.. Encounter an issue while parsing jar: " + e.toString());
        }
        return classToReturn;
    }

}
