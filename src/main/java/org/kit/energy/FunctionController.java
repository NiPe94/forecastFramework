package org.kit.energy;

import org.apache.spark.mllib.tree.configuration.Algo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        linRegCSV.startHere();
        return mav;
    }

    @GetMapping("/run")
    public String runForm(Model model){
        model.addAttribute("algorithm", new Algorithm());
        return "runForm";
    }

    @PostMapping("/run")
    public String submitRunForm(@ModelAttribute Algorithm algorithm){
        return "result";
    }


}
