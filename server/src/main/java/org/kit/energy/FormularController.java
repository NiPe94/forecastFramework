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
 * Controller to handle URLs from the generated web ui, to add data to the spark environment class and
 * to start forecasts or model trainings.
 */
@Controller
public class FormularController {

    /**
     * Object to process a forecast or model training.
     * @see ForecastPipeline
     */
    @Autowired
    private ForecastPipeline modelingPipe;

    /**
     * Object to get time series from a OpenTSDB database.
     * @see TSDBService
     */
    @Autowired
    private TSDBService tsdbService;

    /**
     * Object to generate a json out of a forecast object which will be displayed on the web ui after performing a forecast or model training.
     * @see JSONWriter
     */
    @Autowired
    private JSONWriter jsonWriter;

    /**
     * Factory for registering and creating forecast algorithms during runtime.
     * @see AlgorithmFactory
     */
    @Autowired
    private AlgorithmFactory algorithmFactory;

    /**
     * Object to get access to spark and to hold loaded time series.
     * @see SparkEnvironment
     */
    private SparkEnvironment sparkEnvironment;

    /**
     * GET Request with URL "/stopSpark" to stop the currently running spark environment.
     * @see SparkEnvironment
     */
    @GetMapping(value="/stopSpark")
    public ResponseEntity<?> stopSpark(){
        if(sparkEnvironment != null){
            sparkEnvironment.stopSpark();
        }
        sparkEnvironment = null;
        return ResponseEntity.ok("stopped Spark now");
    }

    /**
     * POST Request with URL "/addData" to upload time series to the spark environment.
     * Therefore the metadata from the web ui is parsed and then a DataFrame object is generated out of it.
     * @param ajaxString The String created from the web ui with metadata about a time series to upload.
     */
    @PostMapping(value = "/addData")
    public ResponseEntity<?> addData(@Valid @RequestBody String ajaxString){

        Map<String,Class<?>> justToUpdate = algorithmFactory.getRegisteredAlgos();

        if(sparkEnvironment == null){
            return new ResponseEntity<String>("No Spark available", HttpStatus.NOT_ACCEPTABLE);
        }

        // parse input data => InputFile
        DataInputParser dataInputParser = new DataInputParser();

        InputFile fileToLoad = null;
        try{
            fileToLoad = dataInputParser.parseInput(ajaxString);
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
            String jsonData = tsdbService.getIt(fileToLoad.getDataPath());
            TSDBFile tsdbFile = new TSDBFile();
            tsdbFile.setDataPurpose(fileToLoad.getDataPurpose());
            tsdbFile.setDataPath(fileToLoad.getDataPath());
            tsdbFile.setJsonData(jsonData);
            fileToLoad = tsdbFile;
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

        return ResponseEntity.ok(ajaxString);
    }

    /**
     * POST request with URL "/startSpark" to start a new local spark environment or to get access to an existing one.
     * @see SparkEnvironment
     * @param ajaxString The String created from the web ui containing a url to a spark master node.
     */
    @PostMapping(value = "/startSpark")
    public ResponseEntity<?> loadSpark(@Valid @RequestBody String ajaxString) {

        String sparkURL = ajaxString
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


        return ResponseEntity.ok(ajaxString);
    }

    /**
     * POST Request with URL "/deleteData" to delete all currently uploaded data from the spark environment.
     */
    @PostMapping(value = "/deleteData")
    public ResponseEntity<?> deleteData() {
        if(sparkEnvironment == null){
            return new ResponseEntity<String>("Failed to delete data", HttpStatus.NOT_ACCEPTABLE);
        }
        sparkEnvironment.deleteData();
        return ResponseEntity.ok("");
    }

    /**
     * POST Request with URL "/" and action parameter to reload the list of currently available forecast algorithms on teh web ui.
     * @param model The model object from Spring Boot which will automatically be injected in this method and hold relevant objects for the web ui to be rendered.
     * @return the thymeleaf template "ForecastFormularMenue"  which has the new algorithm list inside.
     * @see Model
     */
    @PostMapping(value = "/", params = "action=reloadAlgorithms")
    public String reloadAlgorithms(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());
        model.addAttribute("meta",new WrapperDatasetMetadata());

        return "ForecastFormularMenue";
    }

    @GetMapping("/test")
    public ResponseEntity<?> testPreperator() {
        return ResponseEntity.ok("test");
    }

    /**
     * GET Request with URL "/" to load the web ui.
     * @param model The model object from Spring Boot which will automatically be injected in this method and hold relevant objects for the web ui to be rendered.
     * @return the thymeleaf template "ForecastFormularMenue"  which holds and renders relevant metadata objects.
     */
    @GetMapping("/")
    public String indexForm(Model model) {

        model.addAttribute("forecast", new Forecast());
        model.addAttribute("algoList",algorithmFactory.getForecastAlgorithms());
        model.addAttribute("meta",new WrapperDatasetMetadata());

        return "ForecastFormularMenue";
    }

    /**
     * POST Request with URL "/" and action parameter to start a forecast or model training.
     * @param forecast The forecast meta data object
     * @param meta The meta data object to hold the metadata from all uploaded time series, so it can be displayed on the web ui.
     * @param myWrapper The forecast algorithm to be loaded, choosen by a user on the web ui.
     * @param model The model object from Spring Boot which will automatically be injected in this method and hold relevant objects for the web ui to be rendered.
     */
    @PostMapping(value="/",params = "action=perform")
    public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("meta") WrapperDatasetMetadata meta, @ModelAttribute("wrapper") ForecastAlgorithm myWrapper, Model model) {

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

    /**
     * GET Request with URL "/parameters/" followed by a algorithm name to load all parameters from an selected algorithm.
     * Therefore a thymeleaf fragment is used in order to render the parameters in a html page.
     * @param algoName The name of the selected algorithm from the web ui.
     */
    @GetMapping("/parameters/{algoName}")
    public String getParametersForAlgorithm(Model model, @PathVariable("algoName") String algoName){

        ForecastAlgorithm forecastAlgorithm = new ForecastAlgorithm();
        forecastAlgorithm.setAlgoName(algoName);
        forecastAlgorithm.setAlgoParameters(algorithmFactory.getParametersForName(algoName));

        model.addAttribute("wrapper",forecastAlgorithm);

        return "parameters :: parameterList";
    }

    /**
     * GET Request with URL "/inputTypeOptions/" followed by a data type (csv or tsdb).
     * Via a thymeleaf fragment, the relevant data parameters, corresponding to a selected data type by a user, are getting loaded on the web ui.
     * Here the data parameters for the "Label Data" and "Feature Data" sections of the "Load Data" tab are getting loaded.
     */
    @GetMapping("/inputTypeOptions/{datatype}")
    public String getDataTypeOptions(Model model, @PathVariable("datatype") String datatype){

        String fragmentString = "inputOptions :: "+datatype;
        return fragmentString;
    }

    /**
     * GET Request with URL "/additionalInputs/" followed by a data type (csv or tsdb).
     * Via a thymeleaf fragment, the relevant data parameters, corresponding to a selected data type by a user, are getting loaded on the web ui.
     * Here the data parameters for the "Additional Feature Data" section of the "Load Data" tab is getting loaded.
     */
    @GetMapping("/additionalInputs/{datatype}")
    public String getAdditionalInputs(Model model, @PathVariable("datatype") String datatype){
        String fragmentString = "additionalInputs :: "+datatype;
        return fragmentString;
    }

    /**
     * GET Request with URL "/plugins/" to load all available algorithms without using the web ui.
     * @return a list with currently available algorithms.
     */
    @GetMapping(value = "/plugins", produces = {"application/json","text/xml"}, consumes = MediaType.ALL_VALUE)
    public @ResponseBody List<ForecastAlgorithm> getAllPlugins(){
        List<ForecastAlgorithm> forecastAlgorithms = algorithmFactory.getForecastAlgorithms();
        return forecastAlgorithms;
    }

    /**
     * GET Request with URL "/plugins/" followed by an algorithm name to load a specific available algorithm without using the web ui.
     * @return a specific currently available algorithm.
     * @param pluginName The name of the algorithm to be returned.
     */
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

}
