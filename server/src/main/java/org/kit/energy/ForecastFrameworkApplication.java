package org.kit.energy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class which starts Spring Boot and its embedded Tomcat server
 * @author Nico Peter
 * @version 1.0
 */
@SpringBootApplication
public class ForecastFrameworkApplication{

    /**
     * Main program
     *
     * @param args command line arguments
     */
	public static void main(String[] args) {
		SpringApplication.run(ForecastFrameworkApplication.class, args);
	}

}
