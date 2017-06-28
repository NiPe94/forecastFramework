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
        System.out.println();

        for( Class<? extends AlgoPlugin> thing : subtypes){

            // register class names in map
            System.out.println("********************");
            System.out.println(thing.getSimpleName());
            System.out.println("********************");
            System.out.println();

            algorithmFactory.registerAlgo(thing.getSimpleName(),thing);
            algorithmNameList.add(thing.getSimpleName());

            // with annotations:
            System.out.println("fields:");
            System.out.println();

            ArrayList<AlgoParam> paraList = new ArrayList<>();
            ArrayList<AlgoParameter> parameterList = new ArrayList<>();

            Set<Field> fields = getAllFields(thing, withAnnotation(AlgoParam.class));

            if(!fields.isEmpty()){
                System.out.println("First fields as usual: ");
                for(Field f:fields){
                    // get current Annotation
                    AlgoParam algoParam = f.getAnnotation(AlgoParam.class);

                    // create and set Parameter object with values from the annotation
                    AlgoParameter algoParameter = new AlgoParameter();
                    algoParameter.setName(algoParam.name().toString());
                    algoParameter.setValue(algoParam.value().toString());

                    System.out.println("the new algo parameter:");
                    System.out.println(algoParameter.toString());

                    // add the paras to their lists
                    parameterList.add(algoParameter);
                    paraList.add(algoParam);

                    System.out.println("name: "+ f.getName()+" value: "+algoParam.name());
                    System.out.println();
                }
                algorithmToParameterListMap.put(thing.getSimpleName(),parameterList);
            }

        }

        System.out.println("The filled Map from factory");
        System.out.println();
        System.out.println(algorithmFactory.getRegisteredAlgos().toString());
    }
}
