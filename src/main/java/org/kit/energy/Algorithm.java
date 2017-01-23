package org.kit.energy;

/**
 * Created by qa5147 on 23.01.2017.
 */
public class Algorithm {

    private String dataPath;
    private String savePath;
    private AlgorithmType algoType;

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
}
