package org.kit.energy;

import com.google.gson.Gson;
import org.apache.catalina.webresources.ClasspathURLStreamHandler;
import org.reflections.Reflections;
import static org.reflections.ReflectionUtils.*;

import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sun.reflect.Reflection;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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
    private AlgorithmFactory algorithmFactory = new AlgorithmFactory();

    private MapWrapper wrapper = new MapWrapper();

    @GetMapping("/")
    public String indexForm(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("csvfile", new CSVFile());
        model.addAttribute("modeling", new Modeling());
        searchClasses();

        model.addAttribute("map",wrapper.getMap());
        model.addAttribute("selectedAlgoResult", new SelectedAlgo());

        //poster.getIt();
        model.addAttribute("algoParaList",new ArrayList<AlgoParam>());

        return "ForecastFormular";
    }

    public void searchClasses(){
        Reflections reflections = new Reflections("org.kit.energy", new FieldAnnotationsScanner(), new SubTypesScanner());
        Set<Class<? extends AlgoPlugin>> subtypes = reflections.getSubTypesOf(AlgoPlugin.class);
        System.out.println();

        for( Class<? extends AlgoPlugin> thing : subtypes){

            // register class names in map
            System.out.println("********************");
            System.out.println(thing.getSimpleName());
            System.out.println("********************");
            System.out.println();
            algorithmFactory.registerAlgo(thing.getSimpleName(),thing);

            // with annotations:
            System.out.println("fields:");
            System.out.println();


            Set<Field> fields = getAllFields(thing, withAnnotation(AlgoParam.class));

            List<AlgoParam> paraList = new ArrayList<>();

            System.out.println("First fields as usual: ");
            for(Field f:fields){
                AlgoParam algoParam = f.getAnnotation(AlgoParam.class);
                paraList.add(algoParam);
                System.out.println("name: "+ f.getName()+" value: "+algoParam.name());
                System.out.println();
            }

            wrapper.getMap().put(thing.getSimpleName(),paraList);

        }

        System.out.println("The filled Map from factory");
        System.out.println();
        System.out.println(algorithmFactory.getRegisteredAlgos().toString());

        System.out.println("The filled ParaMap");
        System.out.println();
        System.out.println(wrapper.getMap().toString());


        int bla = 98;


    }

    @PostMapping("/")
    public String submitTestForm(@ModelAttribute("forecast") Forecast forecast, @ModelAttribute("algoParaList") ArrayList<AlgoParam> myParas, @ModelAttribute("csvfile") CSVFile fileCSV, @ModelAttribute("modeling") Modeling modeling, @ModelAttribute("wrapper") MapWrapper wrapper, @ModelAttribute("selectedAlgoResult") SelectedAlgo selectedAlgo, Model model, BindingResult bindResult) {

        System.out.println("Start POSTing");
        System.out.println();

        System.out.println("selected Algo: " + selectedAlgo.getSelectedAlgoName());

        System.out.println("die paras: ");
        System.out.println(myParas.toString());

        // test whether the paras got set
        if(wrapper.getMap().isEmpty()){
            System.out.println("what?");
        }
        System.out.println("Start iteration");
        for(Map.Entry<String,List<AlgoParam>> entry : wrapper.getMap().entrySet()){
            System.out.println(entry.getKey()+" : "+entry.getValue().toString());
        }
        System.out.println("End iteration");

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

    @GetMapping("/parameters/{algoName}")
    public String getParametersForAlgorithm(Model model, @PathVariable("algoName") String algoName){

        boolean parasRequested = true;

        model.addAttribute("parasRequested", parasRequested);

        model.addAttribute("algoParaList", wrapper.getMap().get(algoName)); //model.addAttribute("algoParaList", wrapper.getMap().get(algoName));

        return "parameters :: parameterList";
    }

}
