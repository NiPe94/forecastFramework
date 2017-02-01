package org.kit.energy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Ein Prototyp für Energie-Prognosen in Java.
 * Diese Klasse repräsentiert den Einstiegspunkt und dient als Controller im MVC-Kontext
 *
 * @author Nico Peter
 * @version 1.0
 */
@SpringBootApplication
@Controller
public class ForecastFrameworkApplication{
    /**
     * Hauptprogramm
     *
     * @param args Kommandozeilenparameter
     */
	public static void main(String[] args) {
		SpringApplication.run(ForecastFrameworkApplication.class, args);
	}

	@RequestMapping("/page1")
	public String home(Model model) {
		return "page1";
	}

	/*
	@RequestMapping({"/test", "/test/**"})
	public String home(HttpServletRequest request, Model model) {
		model.addAttribute("path", request.getServletPath());
		return "test";
	}
	*/

	/**
	* logout Methode
	*/
	@RequestMapping(value = "/logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/";
	}
}
