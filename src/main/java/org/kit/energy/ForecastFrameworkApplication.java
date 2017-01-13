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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootApplication
@Controller
public class ForecastFrameworkApplication extends WebSecurityConfigurerAdapter{

	public static void main(String[] args) {
		SpringApplication.run(ForecastFrameworkApplication.class, args);
	}

	@Autowired
	private SparkModel exampleModel;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
				.withUser("user")
				.password("password")
				.roles("USER");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers("/test").permitAll()
				.antMatchers("/page1").hasRole("USER")
				.and().formLogin();
	}

	@RequestMapping("/page1")
	public String home(Model model) {
		return "page1";
	}

	@RequestMapping({"/test", "/test/**"})
	public String home(HttpServletRequest request, Model model) {
		model.addAttribute("path", request.getServletPath());
		return "test";
	}

	@RequestMapping("/example")
	public String example(Model model) {
		exampleModel.doStuff();
		model.addAttribute("path", "Shit just got real!");
		return "test";
	}

	@RequestMapping(value = "/logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/";
	}
}
