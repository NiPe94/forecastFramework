package org.kit.energy;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.kit.energy.api.AlgoPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Created by qa5147 on 23.01.2017.
 */
@Controller
public class FormularController {


    @Autowired
    private ForecastPipeline modelingPipe;

    @Autowired
    private TelnetPostToTSDB poster;

    @Autowired
    private JSONWriter jsonWriter;

    @Autowired
    private AlgorithmFactory algorithmFactory;

    private SparkEnvironment sparkEnvironment;

    @GetMapping(value="/stopSpark")
    public ResponseEntity<?> stopSpark(){
        if(sparkEnvironment != null){
            sparkEnvironment.stopSpark();
        }
        sparkEnvironment = null;
        return ResponseEntity.ok("stopped Spark now");
    }

    @PostMapping(value = "/addData")
    public ResponseEntity<?> addData(@Valid @RequestBody String myString){

        Map<String,Class<?>> bla = algorithmFactory.getRegisteredAlgos();

        if(sparkEnvironment == null){
            return new ResponseEntity<String>("No Spark available", HttpStatus.NOT_ACCEPTABLE);
        }

        // parse input data => InputFile
        DataInputParser dataInputParser = new DataInputParser();

        InputFile fileToLoad = null;
        try{
            fileToLoad = dataInputParser.parseInput(myString);
        }catch(Exception parserE){
            System.out.println(parserE.toString());
            return ResponseEntity.badRequest().build();
        }

        if(fileToLoad == null){
            return new ResponseEntity<String>("format error", HttpStatus.NOT_ACCEPTABLE);
        }

        // Put the InputFile into a data preperator => DF with relevant values
        DataPreperator dataPreperator = null;
        if(fileToLoad.getClass() == CSVFile.class){
            dataPreperator = new CSVDataPreperator();
        }
        if(fileToLoad.getClass() == TSDBFile.class){
            dataPreperator = new JSONDataPreperator();
        }
        Dataset<Row> dataset = null;
        try {
            dataset = dataPreperator.prepareDataset(fileToLoad,sparkEnvironment.getInstance());
        } catch (Exception e){
            System.out.println(e.toString());
            return ResponseEntity.badRequest().build();
        }


        // Put the DF into the current spark Environment with info if it's a feature or label
        sparkEnvironment.addData(dataset,fileToLoad.getDataPurpose());

        return ResponseEntity.ok(myString);
    }

    @PostMapping(value = "/startSpark")
    public ResponseEntity<?> loadSpark(@Valid @RequestBody String myString) {

        String sparkURL = myString
                .replace("=","")
                .replace("%5B","[")
                .replace("%5D","]");

        if(sparkURL.equals("")){
            sparkURL = "local";
        }
        if(sparkEnvironment != null){
            System.out.println("trying to stop spark");
            sparkEnvironment.stopSpark();
        }
        try{
            sparkEnvironment = new SparkEnvironment(sparkURL);
            SparkSession sparkSession = sparkEnvironment.getInstance();
            System.out.println("the current spark url: "+sparkURL);
            System.out.println("the current spark version: "+sparkSession.version());
        }catch(Exception sparkExc){
            sparkEnvironment = null;
            System.out.println(sparkExc.toString());
            return ResponseEntity.notFound().build();
        }


        return ResponseEntity.ok(myString);
    }

    @PostMapping(value = "/deleteData")
    public ResponseEntity<?> deleteData() {
        if(sparkEnvironment == null){
            return new ResponseEntity<String>("Failed to delete data", HttpStatus.NOT_ACCEPTABLE);
        }
        sparkEnvironment.deleteData();
        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/", params = "action=reloadAlgorithms")
    public String reloadAlgorithms(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());
        model.addAttribute("meta",new WrapperDatasetMetadata());

        return "ForecastFormularMenue";
    }

    @PostMapping("/test")
    public ResponseEntity<?> testPreperator(@Valid @RequestBody Forecast forecast) {

        System.out.println("the data got: ");
        System.out.println(forecast.toString());

        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/")
    public String indexForm(Model model) {

        model.addAttribute("forecast", new Forecast());
        model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());
        model.addAttribute("meta",new WrapperDatasetMetadata());

        //poster.getIt();

        return "ForecastFormularMenue";
    }

    //@PostMapping(value="/",params = "action=perform")
    //public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("meta") WrapperDatasetMetadata meta, @ModelAttribute("wrapper") ForecastAlgorithm myWrapper, Model model, BindingResult bindResult) {


    @PostMapping(value="/",params = "action=perform")
    public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("meta") WrapperDatasetMetadata meta, @ModelAttribute("wrapper") ForecastAlgorithm myWrapper, Model model, BindingResult bindResult) {

        System.out.println("forecast is getting started");

        // some vars
        boolean modellingDone = false;
        String modelParameters = "";
        String[] modelParametersArray;

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

        if(sparkEnvironment.getInstance()==null){
            modellingDone = true;
            model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());
            model.addAttribute("modellingDone", modellingDone);
            return "ForecastFormularMenue";
        }

        // start the algorithm
        modelParameters = modelingPipe.startForecasting(forecast, algoPlugin, startModeling, startApplication, sparkEnvironment);

        // save the parameters
        modelParametersArray = modelParameters.split(" ");

        String sparkURL = forecast.getSparkURL();
        if(sparkURL.equals("")) {
            sparkURL = "local[*]";
            forecast.setSparkURL(sparkURL);
        }
        forecast.getModeling().setModelParameters(modelParametersArray);
        forecast.setNameOfUsedAlgorithm(algoPlugin.getClass().getSimpleName());

        // parse metadata to set forecasts' dataset metadata
        DataInputParser dataInputParser = new DataInputParser();
        String[] metadataLines = meta.getMetadata().split("\n");
        for(String currentString : metadataLines){
            InputFile fileToShow = dataInputParser.parseInput(currentString);
            String purpose = fileToShow.getDataPurpose();
            if(purpose.equals("feature")){
                forecast.addFeatureFile(fileToShow);
            }
            if(purpose.equals("label")){
                forecast.setLabelFile(fileToShow);
            }
        }

        forecast.setResult(jsonWriter.writeJSON(forecast));


        modellingDone = true;

        model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());
        model.addAttribute("modellingDone", modellingDone);

        return "ForecastFormularMenue";
    }

    @GetMapping("/parameters/{algoName}")
    public String getParametersForAlgorithm(Model model, @PathVariable("algoName") String algoName){

        ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();
        forecastAlgorithm.setAlgoName(algoName);
        forecastAlgorithm.setAlgoParameters(algorithmFactory.getParametersForName(algoName));

        model.addAttribute("wrapper",forecastAlgorithm);

        return "parameters :: parameterList";
    }

    @GetMapping("/inputTypeOptions/{datatype}")
    public String getDataTypeOptions(Model model, @PathVariable("datatype") String datatype){

        String fragmentString = "inputOptions :: "+datatype;
        return fragmentString;
    }

    @GetMapping("/additionalInputs/{datatype}")
    public String getAdditionalInputs(Model model, @PathVariable("datatype") String datatype){
        String fragmentString = "additionalInputs :: "+datatype;
        return fragmentString;
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
