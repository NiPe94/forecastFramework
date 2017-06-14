package org.kit.energy;

import org.kit.energy.IAIAlgorithm;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qa5147 on 13.06.2017.
 */
@Component
public class AlgorithmFactory {
    private Map<String, Class<IAIAlgorithm>> registeredAlgos = new HashMap();

    public void registerAlgo(String name, Class algoClass){
        registeredAlgos.put(name,algoClass);
    }

    public Map<String, Class<IAIAlgorithm>> getRegisteredAlgos(){
        return registeredAlgos;
    }

    /*
    public IAIAlgorithm createAlgo(String name){
        Class algoClass = (Class)registeredAlgos.get(name);
        IAIAlgorithm myAlgo = (IAIAlgorithm) algoClass.newInstance();
        return myAlgo;
    }
    */
}
