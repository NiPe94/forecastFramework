package org.kit.energy;

/**
 * Class to read the input time series metadata from the ajax requests
 */
public class DataInputParser {

    /**
     * Gets a object with metadata about a input time series.
     * @param jsonString the json string from a ajax request which holds the metadata of a input time series.
     * @return the input time series which will be uploaded to the spark environment
     * @see InputFile
     */
    public InputFile parseInput(String jsonString){

        jsonString = jsonString.replace("%2C",",");

        InputFile inputFile = null;
        int beginning, end;

        // extract purpose ()
        beginning = jsonString.indexOf("as=");
        end = jsonString.indexOf("&",beginning);
        String dataPurpose = jsonString.substring(beginning+3,end);

        if(jsonString.contains("type=csv")){
            CSVFile csvFile = new CSVFile();

            csvFile.setDataType("csv");

            // extract header
            beginning = jsonString.indexOf("head=");
            end = jsonString.indexOf("&",beginning);
            csvFile.setHasHeader(false);
            if(jsonString.substring(beginning+5,end).contains("true")){
                csvFile.setHasHeader(true);
            }

            // extract delimeter
            beginning = jsonString.indexOf("delimeter=");
            end = jsonString.indexOf("&",beginning);
            csvFile.setDelimeter(jsonString.substring(beginning+10,end));

            // extract indices
            beginning = jsonString.indexOf("indices=");
            end = jsonString.indexOf("&",beginning);
            if(dataPurpose.contains("label") && !checkFormat(jsonString.substring(beginning+8,end))){
                return inputFile;
            }
            csvFile.setIndices(jsonString.substring(beginning+8,end));

            // extract dataPath
            beginning = jsonString.indexOf("path=");
            end = jsonString.indexOf("&",beginning);
            String dataPath = jsonString.substring(beginning+5,end).replaceAll("%2F","/");
            dataPath = dataPath.replace("%3A",":");
            dataPath = dataPath.replace("%5C","/");
            if(!dataPath.endsWith(".csv")){
                dataPath = dataPath+".csv";
            }
            csvFile.setDataPath(dataPath);

            csvFile.setDataPurpose(dataPurpose);
            inputFile = csvFile;

        }
        else if(jsonString.contains("type=tsdb")){
            TSDBFile tsdbFile = new TSDBFile();
            tsdbFile.setDataType("tsdb");

            // extract metric
            beginning = jsonString.indexOf("metric=");
            end = jsonString.indexOf("&",beginning);
            String metric = jsonString.substring(beginning+7,end);

            // extract tags
            beginning = jsonString.indexOf("tags=");
            String tags = jsonString.substring(beginning+5).replaceAll("%3D","=");
            tags = tags.replace(" ","");

            // extract from
            beginning = jsonString.indexOf("from=");
            end = jsonString.indexOf("&",beginning);
            String fromDate = jsonString.substring(beginning+5,end);

            // extract to
            beginning = jsonString.indexOf("to=");
            end = jsonString.indexOf("&",beginning);
            String toDate = jsonString.substring(beginning+3,end);

            // extract and create final path
            beginning = jsonString.indexOf("path=");
            end = jsonString.indexOf("&",beginning);
            String originalPath = jsonString.substring(beginning+5,end).replaceAll("%2F","/");
            originalPath = originalPath.replace("%3A",":");
            if(!originalPath.endsWith("/")){
                originalPath = originalPath + "/";
            }
            String finalPath = originalPath + "api/query?start="+fromDate+"&end="+toDate+"&m=avg:"+metric+"&{"+tags+"}";

            tsdbFile.setDataPath(finalPath);

            tsdbFile.setDataPurpose(dataPurpose);
            inputFile = tsdbFile;

        }

        return  inputFile;
    }

    private boolean checkFormat(String in){

        try {
            Integer.parseInt(in);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
