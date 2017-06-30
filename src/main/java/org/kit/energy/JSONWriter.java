package org.kit.energy;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by qa5147 on 08.06.2017.
 */
@Component
public class JSONWriter {
    public String writeJSON(Forecast forecast){
        Gson gson = new Gson();
        String resultString = gson.toJson(forecast);

        //2. Convert object to JSON string and save into a file directly
        //String completePath = forecast.getModeling().getSavePathModel() + forecast.getModeling().getAlgoType() + ".JSON";
        String completePath = forecast.getModeling().getSavePathModel() +".JSON";
        try (FileWriter writer = new FileWriter(completePath)) {

            gson.toJson(forecast, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultString;
    }
}
