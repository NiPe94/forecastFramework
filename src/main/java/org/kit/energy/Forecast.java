package org.kit.energy;

import java.util.Collection;

/**
 * Created by qa5147 on 23.01.2017.
 */
public class Forecast {

    private String dataPath;
    private String savePath;
    private AlgorithmType algoType;
    private String[] modelParameters;
    private String result;
    private Integer horizon;

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public AlgorithmType getAlgoType() {
        return algoType;
    }

    public void setAlgoType(AlgorithmType algoType) {
        this.algoType = algoType;
    }

    public String[] getModelParameters() {
        return modelParameters;
    }

    public void setModelParameters(String[] modelParameters) {
        this.modelParameters = modelParameters;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getHorizon() {
        return horizon;
    }

    public void setHorizon(Integer horizon) {
        this.horizon = horizon;
    }
}
