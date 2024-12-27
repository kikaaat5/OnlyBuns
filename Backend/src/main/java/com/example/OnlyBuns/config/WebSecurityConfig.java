package com.example.OnlyBuns.config;

import com.example.OnlyBuns.security.auth.LoginRateLimitingFilter;
import com.example.OnlyBuns.security.auth.RestAuthenticationEntryPoint;
import com.example.OnlyBuns.security.auth.TokenAuthenticationFilter;
import com.example.OnlyBuns.service.impl.UserServiceImpl;
import com.example.OnlyBuns.util.TokenUtils;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


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
	public RateLimiter loginRateLimiter() {
		// Konfiguracija RateLimiter-a sa potrebnim parametrima
		RateLimiterConfig config = RateLimiterConfig.custom()
				.limitForPeriod(5)  // Limitiraj na 1 zahtev po periodu
				.limitRefreshPeriod(java.time.Duration.ofSeconds(60))  // Period osvežavanja
				.timeoutDuration(java.time.Duration.ofMillis(0))  // Timeout na 0 ms
				.build();
		return RateLimiterRegistry.of(config).rateLimiter("loginRateLimiter");
	}

	@Bean
 	public DaoAuthenticationProvider authenticationProvider() {
 	    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
 	    authProvider.setUserDetailsService(userService());
 	    authProvider.setPasswordEncoder(passwordEncoder());

 	    return authProvider;
 	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOrigin("http://localhost:4200");
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
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
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(authorize -> authorize
								.requestMatchers(HttpMethod.DELETE, "/api/posts/{postId}").permitAll()
								.requestMatchers(HttpMethod.PUT, "/api/posts/{postId}").permitAll()
						.requestMatchers("/signin", "/signup", "/auth/**").permitAll()
						.requestMatchers("/auth/login").permitAll()
						.requestMatchers("/api/foo").permitAll() // Dozvoljavaš ove rute bez autentifikacije
						.requestMatchers("/api/clients").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/clients/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/posts/posts/**").permitAll()
						.requestMatchers("/api/posts").permitAll()
						.anyRequest().authenticated()  // Sve ostale rute zahtevaju autentifikaciju
				)
				.httpBasic(Customizer.withDefaults())  // Omogućava osnovnu autentifikaciju
				.formLogin(Customizer.withDefaults())  // Omogućava formu za prijavu

				.addFilterBefore(new LoginRateLimitingFilter(loginRateLimiter()), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new TokenAuthenticationFilter(tokenUtils, userService()), BasicAuthenticationFilter.class)
				.logout(logout -> logout
						.logoutUrl("/signout")
						.logoutSuccessUrl("/signin")
						.invalidateHttpSession(true)
						.permitAll()
				);

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()

				//.requestMatchers(HttpMethod.POST, "/auth/login")
				.requestMatchers(HttpMethod.GET, "/", "/webjars/**", "/*.html", "favicon.ico",
						"/*/*.html", "/*/*.css", "/*/*.js");

	}

}
