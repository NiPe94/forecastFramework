package org.kit.energy;

import java.util.ArrayList;

/**
 * POJO for an forecast algorithm loaded by the AlgorithmSearcher class
 */
public class ForecastAlgorithm {
    String algoName;
    ArrayList<AlgoParameter> algoParameters;

    public String getAlgoName() {
        return algoName;
    }

    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    public ArrayList<AlgoParameter> getAlgoParameters() {
        return algoParameters;
    }

    public void setAlgoParameters(ArrayList<AlgoParameter> algoParameters) {
        this.algoParameters = algoParameters;
    }

    @Override
    public String toString() {
        return "ForecastAlgorithm{" +
                "algoName='" + algoName + '\'' +
                ", algoParameters=" + algoParameters.toString() +
                '}';
    }
}
