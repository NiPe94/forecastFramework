package org.kit.energy;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by qa5147 on 23.01.2017.
 */
@Controller
public class FunctionController {


    @Autowired
    private TestClass testClass;

    @Autowired
    private LinearRegressionLibsvmFormat logRegClass;

    @Autowired
    private LinearRegressionCSVFormat linRegCSV;

    @RequestMapping("/doMethod")
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("greeting");
        //linRegCSV.startHere();
        return mav;
    }

    @GetMapping("/test")
    public String testForm(Model model){
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("csvfile", new CSVFile());
        model.addAttribute("modeling", new Modeling());
        return "testForm";
    }

    @PostMapping("/test")
    public String submitTestForm(@ModelAttribute Forecast forecast, Model model){

        // some vars
        boolean inputError = false;
        boolean modellingDone = false;
        String modelParameters = "";
        String[] modelParametersArray;
        String jsonResult;

        // When file is a dir or does not exist, return to form and display a error bar
        if(checkIfFileIsValid(forecast.getDataPath()) == false) {
            inputError = true;
            model.addAttribute("inputError", inputError);
            model.addAttribute("modellingDone", modellingDone);
            return "testForm";
        }

        model.addAttribute("inputError", inputError);

        if(forecast.getAlgoType() == AlgorithmType.LinearRegressionType) {
            boolean startModeling = true, startApplication = true;
            if(forecast.getPerformType() == PerformType.Modeling){
                startApplication = false;
            }
            if(forecast.getPerformType() == PerformType.Application){
                startModeling = false;
            }
            modelParameters = testClass.start(forecast.getDataPath(), forecast.getSavePathModel(), forecast.getSavePathCSV(), startModeling, startApplication);
            modelParametersArray = modelParameters.split(" ");
            forecast.setModelParameters(modelParametersArray);
            forecast.setResult(writeJSON(forecast));
            modellingDone = true;
        }

        model.addAttribute("inputError", inputError);
        model.addAttribute("modellingDone", modellingDone);
        return "testForm";
    }

    private boolean checkIfFileIsValid(String path){
        File f = new File(path);
        return (f.exists() && !f.isDirectory());
    }

    private String writeJSON(Forecast forecast){
        Gson gson = new Gson();
        String resultString = gson.toJson(forecast);

        //2. Convert object to JSON string and save into a file directly
        String completePath = forecast.getSavePathModel() + forecast.getAlgoType() + ".JSON";
        System.out.println(completePath);
        try (FileWriter writer = new FileWriter(completePath)) {

            gson.toJson(forecast, writer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultString;
    }


}
