package org.kit.energy;

import java.util.ArrayList;

/**
 * Created by qa5147 on 29.06.2017.
 */
public class ForecastAlgorithm {
    String algoName;
    ArrayList<AlgoParameter> algoParameters;
    AlgoPlugin algoPlugin;

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

    public AlgoPlugin getAlgoPlugin() {
        return algoPlugin;
    }

    public void setAlgoPlugin(AlgoPlugin algoPlugin) {
        this.algoPlugin = algoPlugin;
    }
}
