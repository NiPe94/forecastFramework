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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sun.reflect.Reflection;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private AlgorithmFactory algorithmFactory = new AlgorithmFactory();

    @GetMapping("/")
    public String indexForm(Model model) {
        model.addAttribute("forecast", new Forecast());
        model.addAttribute("csvfile", new CSVFile());
        model.addAttribute("modeling", new Modeling());
        searchClasses();
        model.addAttribute("map",algorithmFactory);
        //model.addAttribute("algoList", new AlgoList <= hat List[Algo]);
        //poster.getIt();



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

            Set<Field> fields = reflections.getFieldsAnnotatedWith(AlgoParam.class);

            for(Field f:fields){
                AlgoParam algoParam = f.getAnnotation(AlgoParam.class);
                System.out.println("name: "+ f.getName()+" value: "+algoParam.name());
                System.out.println();
            }

            /*
            // paramNames
            System.out.println("fields:");
            System.out.println();
            Set<Field> myFields = getAllFields(thing,withType(IAIParameter.class));
            for(Field f:myFields){
                System.out.println(f.toString());
                f.setAccessible(true);
                System.out.println("hot:");
                try {
                    System.out.println(f.get(null).toString());
                } catch (Exception e) {
                    System.out.println("Can't get the field");
                    System.out.println("Exception type:");
                    System.out.println(e.getClass().toString());
                    System.out.println();
                }
               
            }
            System.out.println();
            */

        }

        System.out.println("The filled Map from factory");
        System.out.println();
        System.out.println(algorithmFactory.getRegisteredAlgos().toString());


        int bla = 98;

        /*
            System.out.println("lets find the static example:");
            Set<Method> ms = getAllMethods(thing,withModifier(Modifier.STATIC));
            for(Method oneMethod : ms){
                System.out.println(oneMethod.toString());
            }
            System.out.println("Now it burns with field type:");

            Field[] f = thing.getDeclaredFields();
            Field myF = f[0];
            myF.setAccessible(true);
            Class<?> t = myF.getType();
            System.out.println(t);

            if(t == String.class){
                System.out.println("Tell me:");
                System.out.println((String)myF.get(null));
            }

            System.out.println("end statics");
            System.out.println();

            */

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
