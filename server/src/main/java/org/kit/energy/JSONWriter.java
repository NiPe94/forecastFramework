package org.kit.energy;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Class to generate a json out of a Forecast object to be displayed on the browser,
 * after performing a model training or a forecast
 */
@Component
public class JSONWriter {
    public String writeJSON(Forecast forecast){
        Gson gson = new Gson();
        String resultString = gson.toJson(forecast);

        String completePath = forecast.getModeling().getSavePathModel() +".JSON";
        try (FileWriter writer = new FileWriter(completePath)) {

            gson.toJson(forecast, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultString;
    }
}
