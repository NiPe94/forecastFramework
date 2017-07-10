package org.kit.energy;


import java.io.Serializable;
import java.util.Collection;

/**
 * Created by qa5147 on 23.01.2017.
 */
public class Forecast {

    private String savePathCSV;
    private PerformType performType;
    private String result;
    private CSVFile fileCSV;
    private CSVFile labelCSV;
    private Modeling modeling;
    private String sparkURL;

    public String getSavePathCSV() {
        return savePathCSV;
    }

    public void setSavePathCSV(String savePathCSV) {
        this.savePathCSV = savePathCSV;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public PerformType getPerformType() {
        return performType;
    }

    public void setPerformType(PerformType performType) {
        this.performType = performType;
    }

    public CSVFile getFileCSV() {
        return fileCSV;
    }

    public void setFileCSV(CSVFile fileCSV) {
        this.fileCSV = fileCSV;
    }

    public CSVFile getLabelCSV() {
        return labelCSV;
    }

    public void setLabelCSV(CSVFile labelCSV) {
        this.labelCSV = labelCSV;
    }

    public Modeling getModeling() {
        return modeling;
    }

    public void setModeling(Modeling modeling) {
        this.modeling = modeling;
    }

    public String getSparkURL() {
        return sparkURL;
    }

    public void setSparkURL(String sparkURL) {
        this.sparkURL = sparkURL;
    }
}
