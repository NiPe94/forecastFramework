package org.kit.energy;

/**
 * Created by qa5147 on 29.05.2017.
 */
public abstract class InputFile {

    private String dataPath;
    private String dataType;
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
