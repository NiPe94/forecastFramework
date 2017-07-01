package org.kit.energy;

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

    private List<ForecastAlgorithm> forecastAlgorithms;

    private ArrayList<String> algorithmNameList = new ArrayList<>();

    private Map<String,ArrayList<AlgoParameter>> algorithmToParameterListMap = new HashMap<>();

    private AlgorithmFactory algorithmFactory = new AlgorithmFactory();

    public ArrayList<String> getAlgorithmNameList() {
        return algorithmNameList;
    }

    public Map<String, ArrayList<AlgoParameter>> getAlgorithmToParameterListMap() {
        return algorithmToParameterListMap;
    }

    public AlgorithmFactory getAlgorithmFactory() {
        return algorithmFactory;
    }

    public List<ForecastAlgorithm> getForecastAlgorithms() {
        return forecastAlgorithms;
    }

    public void beginSearch(){

        List<ForecastAlgorithm> forecastAlgorithms = new ArrayList<>();


        Reflections reflections = new Reflections("org.kit.energy", new FieldAnnotationsScanner(), new SubTypesScanner());
        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        for( Class<? extends AlgoPlugin> plugin : subtypes){

            // initialize values
            ArrayList<AlgoParam> paraList = new ArrayList<>();
            ArrayList<AlgoParameter> parameterList = new ArrayList<>();
            ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();

            // register plugin in factory
            algorithmFactory.registerAlgo(plugin.getSimpleName(),plugin);
            algorithmNameList.add(plugin.getSimpleName());
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
                algorithmToParameterListMap.put(plugin.getSimpleName(),parameterList);

                forecastAlgorithms.add(forecastAlgorithm);
            }

        }
        this.forecastAlgorithms = forecastAlgorithms;
    }
}
