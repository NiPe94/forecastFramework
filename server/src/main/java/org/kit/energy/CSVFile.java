package org.kit.energy;

import java.io.File;

/**
 * Created by qa5147 on 30.01.2017.
 */
public class CSVFile extends InputFile{

    private boolean hasHeader;
    private String delimeter;
    private String indices;

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
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

    public String getDelimeter(){
        return this.delimeter;
    }

    public String getIndices() {
        return indices;
    }

    public void setIndices(String indices) {
        this.indices = indices;
    }

    @Override
    public String toString() {
        return "path: "+super.getDataPath()
                +" data type: "+super.getDataType()
                +" data purpose: "+super.getDataPurpose()
                +" head: "+this.hasHeader
                +" delimeter: "+this.delimeter
                +" indices: "+this.indices;
    }
}