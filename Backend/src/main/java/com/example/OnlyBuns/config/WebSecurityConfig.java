package com.example.OnlyBuns.config;

import com.example.OnlyBuns.security.auth.RestAuthenticationEntryPoint;
import com.example.OnlyBuns.security.auth.TokenAuthenticationFilter;
import com.example.OnlyBuns.service.impl.UserServiceImpl;
import com.example.OnlyBuns.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Configuration
// Injektovanje bean-a za bezbednost
@EnableWebSecurity
// Ukljucivanje podrske za anotacije "@Pre*" i "@Post*" koje ce aktivirati autorizacione provere za svaki pristup metodi
@EnableMethodSecurity
public class WebSecurityConfig {

	// Servis koji se koristi za citanje podataka o korisnicima aplikacije
	@Bean
    public UserServiceImpl userService() {
        return new UserServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


 	@Bean
 	public DaoAuthenticationProvider authenticationProvider() {
 	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
 	    authProvider.setUserDetailsService(userService());
 	    authProvider.setPasswordEncoder(passwordEncoder());

 	    return authProvider;
 	}

 	@Autowired
 	private RestAuthenticationEntryPoint restAuthenticationEntryPoint;


 	@Bean
 	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
 	    return authConfig.getAuthenticationManager();
 	}

	@Autowired
	private TokenUtils tokenUtils;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
						auth -> auth.requestMatchers("/signin", "/signup", "/auth/**").permitAll()
								.anyRequest().authenticated()
				)
				.addFilterBefore(new TokenAuthenticationFilter(new TokenUtils(), userService()), BasicAuthenticationFilter.class)
				.logout(logout -> logout
						.logoutUrl("/signout")
						.logoutSuccessUrl("/signin")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll()
				);

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
				.requestMatchers(HttpMethod.POST, "/auth/login")
				.requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
						"/**/*.html", "/**/*.css", "/**/*.js");
	}

}
