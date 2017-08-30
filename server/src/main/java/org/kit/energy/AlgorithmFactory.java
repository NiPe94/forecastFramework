package org.kit.energy;

import org.apache.commons.collections.map.HashedMap;
import org.kit.energy.api.AlgoParam;
import org.kit.energy.api.AlgoPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Factory to register and create forecast algorithms which will be used by the webservice.
 */
@Component
public class AlgorithmFactory {

    /**
     * List with objects of type ForecastAlgorithm, generated with the loaded algorithms from the algorithmSearcher.
     * This list will be parsed in the thymeleaf template "ForecastFormularMenue.html"
     * to display all currently available algorithms from a specific directory during runtime.
     * Additionally the list will be used to create an algorithm chosen by a user via the web ui.
     * @see ForecastAlgorithm
     */
    private ArrayList<ForecastAlgorithm> loadedForecastAlgorithms;

    /**
     * Mapping of algorithm names to algorithm classes from the algorithmSearcher.
     * So with a given algorithm name from the web ui, the corresponding class can be instantiated.
     */
    private Map<String, Class<?>> registeredAlgos = new HashMap();

    /**
     * Object to load currently available algorithm classes from a specific directory.
     * @see AlgorithmSearcher
     */
    @Autowired
    private AlgorithmSearcher algorithmSearcher;

    /**
     * Gets the mapping between algorithm names and algorithm classes.
     * @return a mapping between algorithm names and algorithm classes
     */
    public Map<String, Class<?>> getRegisteredAlgos(){
        return registeredAlgos;
    }

    /**
     * creates an instance of the algorithm chosen by a user via the web ui including changed algorithm parameters.
     * @param forecastAlgorithm the algorithm chosen and configured by a user via the web ui.
     * @return the instantiated algorithm class for a forecast or model training.
     * @see AlgoPlugin
     * @throws InstantiationException,IllegalAccessException
     */
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

        // get all annotated fields
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

    /**
     * Gets a list of currently available forecast algorithms with the help of the algorithmSearcher object.
     * Updates the map registeredAlgos and the list loadedForecastAlgorithms.
     * @return a new list with forecast algorithms
     * @see AlgorithmSearcher
     */
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

    /**
     * Gets a list of algorithm parameters for a specific loaded algorithm name.
     * Is used by the controller when parameters got requested directly.
     * @return a list of algorithm parameters.
     * @see AlgoParameter
     */
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
