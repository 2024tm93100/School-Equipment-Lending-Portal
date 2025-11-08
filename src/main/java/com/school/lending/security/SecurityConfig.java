package com.school.lending.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		// Custom JWT role converter
		JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
		jwtAuthConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

		http
				// 🔹 Disable CSRF for simplicity during development
				.csrf(csrf -> csrf.disable())

				// 🔹 Allow H2 Console in a frame (same-origin)
				.headers(headers -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))

				// 🔹 Authorization rules
				.authorizeHttpRequests(auth -> auth
						// Allow public pages and H2 console
						.requestMatchers("/", "/error", "/public/**", "/h2-console/**", "/swagger-ui/**", "/v3/**")
						.permitAll().requestMatchers("/api/register", "/api/token").permitAll()
						// Role-based API restrictions
						.requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/staff/**")
						.hasAnyRole("STAFF", "ADMIN").requestMatchers("/api/student/**")
						.hasAnyRole("STUDENT", "STAFF", "ADMIN")
						// All other requests must be authenticated
						.anyRequest().authenticated())

				// 🔹 Enable OAuth2 login for browser users
				.oauth2Login(Customizer.withDefaults())

				// 🔹 Enable JWT bearer token for REST API calls
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));

		return http.build();
	}
}
