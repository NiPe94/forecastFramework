package org.kit.energy;

/**
 * Created by qa5147 on 29.05.2017.
 */
public abstract class InputFile {

    private String dataPath;
    private String labelColumnIndex;
    private String featureColumnsIndexes;
    private String dataType;

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getLabelColumnIndex() {
        return labelColumnIndex;
    }

    public void setLabelColumnIndex(String labelColumnIndex) {
        this.labelColumnIndex = labelColumnIndex;
    }

    public String getFeatureColumnsIndexes() {
        return featureColumnsIndexes;
    }

    public void setFeatureColumnsIndexes(String featureColumnsIndexes) {
        this.featureColumnsIndexes = featureColumnsIndexes;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
