package org.kit.energy;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.*;

/**
 * Created by qa5147 on 23.01.2017.
 */
@Controller
public class FormularController {


    @Autowired
    private ForecastPipeline modelingPipe;

    @GetMapping("/")
    public String indexForm(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("csvfile", new CSVFile());
        model.addAttribute("modeling", new Modeling());
        //telIt();
        return "ForecastFormular";
    }

    @PostMapping("/")
    public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("csvfile") CSVFile fileCSV, @ModelAttribute("modeling") Modeling modeling, Model model, BindingResult bindResult) {

        // some vars
        boolean modellingDone = false;
        String modelParameters = "";
        String[] modelParametersArray;
        forecast.setFileCSV(fileCSV);
        forecast.setModeling(modeling);

        // validate input data
        Validator validator = new Validator(fileCSV);
        model.addAttribute("validatorError", !validator.isValid());
        model.addAttribute("validatorMessage", validator.getMessage());

        // When file is a dir or does not exist, return to form and display a error bar
        if (!validator.isValid()) {
            model.addAttribute("modellingDone", modellingDone);
            return "ForecastFormular";
        }


        // start selected algorithm. -> Algorithm-Starter-Manager-Class?
        if (modeling.getAlgoType() == AlgorithmType.LinearRegressionType) {
            boolean startModeling = true, startApplication = true;
            // what action will be performed?
            if (forecast.getPerformType() == PerformType.Modeling) {
                startApplication = false;
            }
            if (forecast.getPerformType() == PerformType.Application) {
                startModeling = false;
            }
            // start the algorithm
            modelParameters = modelingPipe.startForecasting(fileCSV.getDataPath(), modeling.getSavePathModel(), forecast.getSavePathCSV(), startModeling, startApplication, fileCSV.isHasHeader(), fileCSV.getDelimeter(), fileCSV.getLabelColumnIndex(), fileCSV.getFeatureColumnsIndexes());
            // save the parameters
            modelParametersArray = modelParameters.split(" ");
            modeling.setModelParameters(modelParametersArray);
            forecast.setResult(writeJSON(forecast));
        }


        modellingDone = true;

        model.addAttribute("modellingDone", modellingDone);

        return "ForecastFormular";
    }

    // write a json out of an object
    private String writeJSON(Forecast forecast) {
        Gson gson = new Gson();
        String resultString = gson.toJson(forecast);

        //2. Convert object to JSON string and save into a file directly
        String completePath = forecast.getModeling().getSavePathModel() + forecast.getModeling().getAlgoType() + ".JSON";
        try (FileWriter writer = new FileWriter(completePath)) {

            gson.toJson(forecast, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return resultString;
    }


    /*
    public void telIt(){
        // POST via telnet, but not so important as getting the data
        System.out.println("Bin schon hier!");
        String[] strArray = new String[0];
        //Example.main(strArray);
        System.out.println("Fertig");
        try(Socket sock = new Socket("localhost", 4242)){
            String point = "put testmetrik 1486538632 155 bla=blub\n";
            sock.getOutputStream().write(point.getBytes());
        }
        catch(IOException ex){
            System.out.println("Böser Fehler!");
        }


        // GET
        try{
            String url = "http://localhost:4242/api/query?start=1486425600&m=sum:testmetrik";
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println("This is what you get man!:");
            System.out.println(response.toString());

        }
        catch(Exception e){

        }

    }
    */



}
