package org.kit.energy;

/**
 * Created by qa5147 on 30.01.2017.
 */
public class CSVFile {

    private String dataPath;
    private boolean hasHeader;
    private String delimeter;
    private int labelColumnIndex;
    private int[] featureColumnsIndexes;

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
        this.delimeter = delimeter;
    }

    public int getLabelColumnIndex() {
        return labelColumnIndex;
    }

    public void setLabelColumnIndex(int labelColumnIndex) {
        this.labelColumnIndex = labelColumnIndex;
    }

    public int[] getFeatureColumnsIndexes() {
        return featureColumnsIndexes;
    }

    public void setFeatureColumnsIndexes(int[] featureColumnsIndexes) {
        this.featureColumnsIndexes = featureColumnsIndexes;
    }
}