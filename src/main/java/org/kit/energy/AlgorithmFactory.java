package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Created by qa5147 on 13.06.2017.
 */
@Component
public class AlgorithmFactory {

    private ArrayList<ForecastAlgorithm> loadedForecastAlgorithms;
    private Map<String, Class<?>> registeredAlgos = new HashMap();
    private AlgorithmSearcher algorithmSearcher = new AlgorithmSearcher();

    // ***********************************************************************

    public Map<String, Class<?>> getRegisteredAlgos(){
        return registeredAlgos;
    }

    public AlgoPlugin createAlgo(ForecastAlgorithm forecastAlgorithm){

        Class algoClass = registeredAlgos.get(forecastAlgorithm.getAlgoName());

        AlgoPlugin algoPlugin = null;

        try {
            algoPlugin = (AlgoPlugin) algoClass.newInstance();
        } catch (InstantiationException e) {
            System.out.println(e.toString());
        } catch (IllegalAccessException e) {
            System.out.println(e.toString());
        }

        Set<Field> fields = getAllFields(algoPlugin.getClass(),withAnnotation(AlgoParam.class));

        String fieldAnnotationName;
        String parameterValue = "";

        // all annotated fields
        for(Field field: fields){
            Class<?> type = field.getType();
            if(type.isAssignableFrom(String.class)){
                field.setAccessible(true);
                fieldAnnotationName = field.getAnnotation(AlgoParam.class).name();
                // get parameter value for parameter name
                for(AlgoParameter algoParameter : forecastAlgorithm.getAlgoParameters()){
                    if(algoParameter.getName().equals(fieldAnnotationName)){
                        parameterValue = algoParameter.getValue();
                    }
                }
                try {
                    field.set(algoPlugin,parameterValue);
                } catch (IllegalAccessException e) {
                    System.out.println("Fehler bei Setting!");
                }
            }
        }
        return algoPlugin;
    }

    public List<ForecastAlgorithm> getForecastAlgorithms(){
        Map<ForecastAlgorithm, Class<?>> loadedPlugins = null;
        try {
            loadedPlugins = algorithmSearcher.beginSearch("org.kit.energy");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Class<?>> registeredAlgos = new HashedMap();
        for(Map.Entry<ForecastAlgorithm, Class<?>> entry : loadedPlugins.entrySet()){
            registeredAlgos.put(entry.getKey().getAlgoName(),entry.getValue());
        }
        this.registeredAlgos = registeredAlgos;
        ArrayList<ForecastAlgorithm> listToReturn = new ArrayList<>();
        listToReturn.addAll(loadedPlugins.keySet());
        this.loadedForecastAlgorithms = listToReturn;
        return listToReturn;
    }

    public ArrayList<AlgoParameter> getParametersForName(String name){
        ArrayList<AlgoParameter> algoParameters = new ArrayList<>();
        for(ForecastAlgorithm forecastAlgorithm : loadedForecastAlgorithms){
            if(forecastAlgorithm.getAlgoName().equals(name)){
                algoParameters = forecastAlgorithm.getAlgoParameters();
            }
        }
        return algoParameters;
    }

}
