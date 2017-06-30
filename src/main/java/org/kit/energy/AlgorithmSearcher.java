package org.kit.energy;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Created by qa5147 on 28.06.2017.
 */
public class AlgorithmSearcher {

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

    public void beginSearch(){
        Reflections reflections = new Reflections("org.kit.energy", new FieldAnnotationsScanner(), new SubTypesScanner());
        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);

        for( Class<? extends AlgoPlugin> thing : subtypes){

            // register class names in map
            /*
            System.out.println("********************");
            System.out.println(thing.getSimpleName());
            System.out.println("********************");
            System.out.println();
            */

            algorithmFactory.registerAlgo(thing.getSimpleName(),thing);
            algorithmNameList.add(thing.getSimpleName());

            // with annotations:
            ArrayList<AlgoParam> paraList = new ArrayList<>();
            ArrayList<AlgoParameter> parameterList = new ArrayList<>();

            Set<Field> fields = getAllFields(thing, withAnnotation(AlgoParam.class));

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
                algorithmToParameterListMap.put(thing.getSimpleName(),parameterList);
            }

        }
    }
}
