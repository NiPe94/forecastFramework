package org.kit.energy;

import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qa5147 on 13.06.2017.
 */
@Component
public class AlgorithmFactory {
    private Map<String, Class<?>> registeredAlgos = new HashMap();

    public void registerAlgo(String name, Class algoClass){
        registeredAlgos.put(name,algoClass);
    }

    public Map<String, Class<?>> getRegisteredAlgos(){
        return registeredAlgos;
    }


    public AlgoPlugin createAlgo(String name){
        Class algoClass = (Class)registeredAlgos.get(name);
        AlgoPlugin myAlgo = null;
        try {
            myAlgo = (AlgoPlugin) algoClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return myAlgo;
    }

}
