package org.kit.energy;

import java.io.File;

/**
 * Created by qa5147 on 31.01.2017.
 */
public class Validator {

    CSVFile file;
    String message;
    boolean isValid;

    public Validator(CSVFile file){
        this.file = file;
        this.message = "";
        this.validate();
    }

    // validate all user input data
    private void validate(){
        boolean test = true;

        // file path test
        if(!this.checkIfFileIsValid(this.file.getDataPath())){
            this.message += "File not found: Please provide a valid path to a file! ";
            test = false;
        }

        // label input test
        if(!this.checkLabelInput(this.file.getLabelColumnIndex())){
            this.message += "Wrong label input: Please type in a number ";
            test = false;
        }

        // features input test
        if(!this.checkFeatureInputFormat(this.file.getFeatureColumnsIndexes())){
            this.message += "Wrong features input: Please type in numbers like: 2,3,4 ";
            test = false;
        }

        this.isValid = test;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean checkIfFileIsValid(String path) {

        File f = new File(path);
        return (f.exists() && !f.isDirectory());
    }

    public boolean checkFeatureInputFormat(String string) {

        String delimeter = ",";
        String[] splittedString;

        if (string.contains(delimeter)) {
            string = string.replace(" ","");
        } else {
            return false;
        }

        try {
            splittedString = string.split(delimeter);
            for(String str:splittedString){
                Integer.parseInt(str);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean checkLabelInput(String string) {

        try {
            int number = Integer.parseInt(string);
        } catch (Exception e) {
            return false;
        }

        return true;
    }


}
