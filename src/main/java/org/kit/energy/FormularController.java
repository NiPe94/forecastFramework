package org.kit.energy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import static org.reflections.ReflectionUtils.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private AlgorithmFactory algorithmFactory;


    @GetMapping("/")
    public String indexForm(Model model) {

        model.addAttribute("forecast", new Forecast());
        model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());

        //poster.getIt();

        return "ForecastFormular";
    }

    @PostMapping("/")
    public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("wrapper") ForecastAlgorithm myWrapper, Model model, BindingResult bindResult) {

        // some vars
        boolean modellingDone = false;
        String modelParameters = "";
        String[] modelParametersArray;

        // validate input data
        Validator validator = new Validator(forecast.getFileCSV());
        model.addAttribute("validatorError", !validator.isValid());
        model.addAttribute("validatorMessage", validator.getMessage());

        // When file is a dir or does not exist, return to form and display a error bar
        if (!validator.isValid()) {
            model.addAttribute("modellingDone", modellingDone);
            return "ForecastFormular";
        }

        // create a forecastAlgorithm and copy its values to the plugin-object, which will be used for the forecast
        AlgoPlugin algoPlugin = algorithmFactory.createAlgo(myWrapper);

        boolean startModeling = true, startApplication = true;

        // what action will be performed?
        if (forecast.getPerformType() == PerformType.Modeling) {
            startApplication = false;
        }
        if (forecast.getPerformType() == PerformType.Application) {
            startModeling = false;
        }

        // start the algorithm
        modelParameters = modelingPipe.startForecasting(forecast, algoPlugin, startModeling, startApplication);

        // save the parameters
        modelParametersArray = modelParameters.split(" ");
        forecast.getModeling().setModelParameters(modelParametersArray);
        forecast.setResult(jsonWriter.writeJSON(forecast));

        modellingDone = true;

        model.addAttribute("modellingDone", modellingDone);

        return "ForecastFormular";
    }

    @GetMapping("/parameters/{algoName}")
    public String getParametersForAlgorithm(Model model, @PathVariable("algoName") String algoName){

        ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();
        forecastAlgorithm.setAlgoName(algoName);
        forecastAlgorithm.setAlgoParameters(algorithmFactory.getParametersForName(algoName));

        model.addAttribute("wrapper",forecastAlgorithm);

        return "parameters :: parameterList";
    }

    @GetMapping(value = "/plugins", produces = {"application/json","text/xml"}, consumes = MediaType.ALL_VALUE)
    public @ResponseBody List<ForecastAlgorithm> getAllPlugins(){
        List<ForecastAlgorithm> forecastAlgorithms = algorithmFactory.getForecastAlgorithms();
        return forecastAlgorithms;
    }

    @GetMapping(value = "/plugins/{pluginName}", produces = {"application/json","text/xml"}, consumes = MediaType.ALL_VALUE)
    public @ResponseBody ForecastAlgorithm getPluginWithName(@PathVariable(value="pluginName") String pluginName){
        List<ForecastAlgorithm> forecastAlgorithms = algorithmFactory.getForecastAlgorithms();
        ForecastAlgorithm forecastAlgorithmToReturn = new ForecastAlgorithm();

        for(ForecastAlgorithm forecastAlgorithm:forecastAlgorithms){
            if(forecastAlgorithm.getAlgoName().equals(pluginName)){
                forecastAlgorithmToReturn = forecastAlgorithm;
            }
        }
        return forecastAlgorithmToReturn;
    }

    @PostMapping(value= "/execute")
    public @ResponseBody String executeWithAlgorithm(@RequestBody ForecastAlgorithm forecastAlgorithm){
        String message = "Did not work!";
        if(forecastAlgorithm != null){
            System.out.println("The Algorithm from the JSON: ");
            System.out.println(forecastAlgorithm);
            message = "It works!";
            // load the current algorithm plugins and search for the name, then start the forecasting
        }
        return message;
    }

}
