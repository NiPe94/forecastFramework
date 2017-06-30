package org.kit.energy;

import java.util.ArrayList;

/**
 * Created by qa5147 on 28.06.2017.
 */
public class WrapperForListOfParameters {
    private ArrayList<AlgoParameter> dadList;

    public ArrayList<AlgoParameter> getDadList() {
        return dadList;
    }

    public void setDadList(ArrayList<AlgoParameter> dadList) {
        this.dadList = dadList;
    }

    public AlgoParameter getParameterWithName(String name){
        AlgoParameter paraToReturn = new AlgoParameter();
        for(AlgoParameter algoParameter : dadList){
            if(algoParameter.getName().equals(name)){
                paraToReturn = algoParameter;
            }
        }
        return paraToReturn;
    }

    @Override
    public String toString() {
        return "WrapperForListOfParameters{" +
                "dadList=" + dadList.toString() +
                '}';
    }
}
