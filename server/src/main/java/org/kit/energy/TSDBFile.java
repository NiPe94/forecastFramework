package org.kit.energy;

/**
 * POJO for metadata from a OpenTSDB json file
 */
public class TSDBFile extends InputFile {
    String jsonData;

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
