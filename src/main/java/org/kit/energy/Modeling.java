package org.kit.energy;

/**
 * Created by qa5147 on 30.01.2017.
 */
public class Modeling {

    private String savePathModel;
    private AlgorithmType algoType;
    private String[] modelParameters;
    private Integer horizon;

    public String getSavePathModel() {
        return savePathModel;
    }

    public void setSavePathModel(String savePathModel) {
        this.savePathModel = savePathModel;
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

    public Integer getHorizon() {
        return horizon;
    }

    public void setHorizon(Integer horizon) {
        this.horizon = horizon;
    }
}
