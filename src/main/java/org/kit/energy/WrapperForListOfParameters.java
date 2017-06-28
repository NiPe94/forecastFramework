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

    @Override
    public String toString() {
        return "WrapperForListOfParameters{" +
                "dadList=" + dadList.toString() +
                '}';
    }
}
