package org.kit.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Ein Prototyp für Energie-Prognosen in Java.
 * Diese Klasse repräsentiert den Einstiegspunkt und dient als Controller im MVC-Kontext
 *
 * @author Nico Peter
 * @version 1.0
 */
@SpringBootApplication
public class ForecastFrameworkApplication{

    /**
     * Hauptprogramm
     *
     * @param args Kommandozeilenparameter
     */
	public static void main(String[] args) {
		SpringApplication.run(ForecastFrameworkApplication.class, args);
	}

}
