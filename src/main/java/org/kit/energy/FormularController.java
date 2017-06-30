package org.kit.energy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import static org.reflections.ReflectionUtils.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    private AlgorithmSearcher algorithmSearcher;


    @GetMapping("/")
    public String indexForm(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("csvfile", new CSVFile());
        model.addAttribute("modeling", new Modeling());
        algorithmSearcher = new AlgorithmSearcher();
        algorithmSearcher.beginSearch();
        model.addAttribute("algoNameList",algorithmSearcher.getAlgorithmNameList());
        model.addAttribute("selectedAlgoResult", new SelectedAlgo());

        //poster.getIt();

        return "ForecastFormular";
    }

    @PostMapping("/")
    public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("wrapper") WrapperForListOfParameters myWrapper, @ModelAttribute("csvfile") CSVFile fileCSV, @ModelAttribute("modeling") Modeling modeling, @ModelAttribute("selectedAlgoResult") SelectedAlgo selectedAlgo, Model model, BindingResult bindResult) {

        System.out.println();
        System.out.println("Start submitting!");
        System.out.println();

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

        String algoName = selectedAlgo.getSelectedAlgoName();
        ArrayList<AlgoParameter> algoParameters = myWrapper.getDadList();
        AlgoPlugin algoPlugin = algorithmSearcher.getAlgorithmFactory().createAlgo(algoName);
        Set<Field> fields = getAllFields(algoPlugin.getClass(),withAnnotation(AlgoParam.class));

        String fieldAnnotationName = "";
        String parameterValue = "";
        for(Field field: fields){
            Class<?> type = field.getType();
            if(type.isAssignableFrom(String.class)){
                field.setAccessible(true);
                fieldAnnotationName = field.getAnnotation(AlgoParam.class).name();
                parameterValue = myWrapper.getParameterWithName(fieldAnnotationName).getValue();
                try {
                    field.set(algoPlugin,parameterValue);
                } catch (IllegalAccessException e) {
                    System.out.println("Fehler bei Setting!");
                }
            }
        }

        ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();
        forecastAlgorithm.setAlgoName(algoName);
        forecastAlgorithm.setAlgoParameters(algoParameters);
        forecastAlgorithm.setAlgoPlugin(algoPlugin);


        boolean startModeling = true, startApplication = true;

        // what action will be performed?
        if (forecast.getPerformType() == PerformType.Modeling) {
            startApplication = false;
        }
        if (forecast.getPerformType() == PerformType.Application) {
            startModeling = false;
        }

        // start the algorithm
        //modelParameters = modelingPipe.startForecasting(fileCSV.getDataPath(), modeling.getSavePathModel(), forecast.getSavePathCSV(), startModeling, startApplication, fileCSV.isHasHeader(), fileCSV.getDelimeter(), fileCSV.getLabelColumnIndex(), fileCSV.getFeatureColumnsIndexes());
        System.out.println();
        System.out.println("Start calling the pipeline!");
        System.out.println();
        modelParameters = modelingPipe.startForecasting(forecast, forecastAlgorithm, startModeling, startApplication);
        // save the parameters
        modelParametersArray = modelParameters.split(" ");
        modeling.setModelParameters(modelParametersArray);
        forecast.setResult(jsonWriter.writeJSON(forecast));



        modellingDone = true;

        model.addAttribute("modellingDone", modellingDone);

        return "ForecastFormular";
    }

    @GetMapping("/parameters/{algoName}")
    public String getParametersForAlgorithm(Model model, @PathVariable("algoName") String algoName){

        WrapperForListOfParameters newMapper = new WrapperForListOfParameters();

        newMapper.setDadList(algorithmSearcher.getAlgorithmToParameterListMap().get(algoName));

        model.addAttribute("wrapper",newMapper);

        return "parameters :: parameterList";
    }

}
