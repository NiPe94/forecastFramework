package org.kit.energy;

import java.io.File;

/**
 * Created by qa5147 on 30.01.2017.
 */
public class CSVFile extends InputFile{

    private boolean hasHeader;

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
        super.setDelimeter(delimeter);
    }


}
