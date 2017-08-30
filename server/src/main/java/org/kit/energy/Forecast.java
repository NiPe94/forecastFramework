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
    private ArrayList<InputFile> featureFiles = new ArrayList<>();
    private InputFile labelFile;
    private Modeling modeling;
    private String sparkURL;
    private ForecastAlgorithm usedAlgorithm;

    public ForecastAlgorithm getUsedAlgorithm() {
        return usedAlgorithm;
    }

    public void setUsedAlgorithm(ForecastAlgorithm usedAlgorithm) {
        this.usedAlgorithm = usedAlgorithm;
    }

    public String getSavePathCSV() {
        return savePathCSV;
    }

    public void setSavePathCSV(String savePathCSV) {
        this.savePathCSV = savePathCSV;
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
