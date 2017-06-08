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

    @Autowired
    private TelnetPostToTSDB poster = new TelnetPostToTSDB();

    @Autowired
    private JSONWriter jsonWriter;

    @GetMapping("/")
    public String indexForm(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("csvfile", new CSVFile());
        model.addAttribute("modeling", new Modeling());
        //poster.telIt();
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
            forecast.setResult(jsonWriter.writeJSON(forecast));
        }


        modellingDone = true;

        model.addAttribute("modellingDone", modellingDone);

        return "ForecastFormular";
    }

}
