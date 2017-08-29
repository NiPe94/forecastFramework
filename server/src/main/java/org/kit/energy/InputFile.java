package org.kit.energy;

/**
 * Interface for different input data sources
 */
public abstract class InputFile {

    private String dataPath;
    private String dataType;

    /**
     * Holds the String "label" or "feature"
     */
    private String dataPurpose;

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataPurpose() {
        return dataPurpose;
    }

    public void setDataPurpose(String dataPurpose) {
        this.dataPurpose = dataPurpose;
    }
}
