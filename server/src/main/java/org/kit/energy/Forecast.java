package org.kit.energy;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * POJO for forecast metadata, which will be filled via the web ui
 */
public class Forecast {

    private String savePathCSV;
    private PerformType performType;
    private String result;
    private ArrayList<InputFile> featureFiles = new ArrayList<>();
    private InputFile labelFile;
    private Modeling modeling;
    private String sparkURL;
    private String nameOfUsedAlgorithm;

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

    public String getNameOfUsedAlgorithm() {
        return nameOfUsedAlgorithm;
    }

    public void setNameOfUsedAlgorithm(String nameOfUsedAlgorithm) {
        this.nameOfUsedAlgorithm = nameOfUsedAlgorithm;
    }

    public ArrayList<InputFile> getFeatureFiles() {
        return featureFiles;
    }

    public void addFeatureFile(InputFile featureFile) {
        this.featureFiles.add(featureFile);
    }

    public InputFile getLabelFile() {
        return labelFile;
    }

    public void setLabelFile(InputFile labelFile) {
        this.labelFile = labelFile;
    }
}
