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
 * Created by qa5147 on 28.06.2017.
 */
@Component
public class AlgorithmSearcher {

    private final String finalPath;

    public AlgorithmSearcher(@Value("${plugin.loading.path}") String path){
        this.finalPath = path;
    }

    public Map<ForecastAlgorithm, Class<?>> beginSearch(String path){

        String templatePackageStructure = "";

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
