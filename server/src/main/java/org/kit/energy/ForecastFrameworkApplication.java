package org.kit.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
