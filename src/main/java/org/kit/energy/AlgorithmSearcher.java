package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Created by qa5147 on 28.06.2017.
 */
@Component
public class AlgorithmSearcher {

    public Map<ForecastAlgorithm, Class<?>> beginSearch(String path){

        path = "org.kit.energy";

        Map<ForecastAlgorithm, Class<?>> forecastAlgorithmsWithPlugins = new HashedMap();

        Reflections reflections = new Reflections(path, new FieldAnnotationsScanner(), new SubTypesScanner());
        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

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
}
