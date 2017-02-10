package org.kit.energy;

import java.io.File;

/**
 * Created by qa5147 on 30.01.2017.
 */
public class CSVFile {

    private String dataPath;
    private boolean hasHeader;
    private String delimeter;
    private String labelColumnIndex;
    private String featureColumnsIndexes;
    private String forecastPastHorizon;

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public String getDelimeter() {
        return delimeter;
    }

    public void setDelimeter(String delimeter) {
        if(Integer.parseInt(delimeter) == 2){
            delimeter = ";";
        }
        else {
            delimeter = ",";
        }
        this.delimeter = delimeter;
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

    public String getForecastPastHorizon() {
        return forecastPastHorizon;
    }

    public void setForecastPastHorizon(String forecastPastHorizon) {
        this.forecastPastHorizon = forecastPastHorizon;
    }
}
