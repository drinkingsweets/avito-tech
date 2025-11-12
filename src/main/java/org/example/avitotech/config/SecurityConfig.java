package org.example.avitotech.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Выключаем CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/team/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/team/get").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/users/setIsActive").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/getReview").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/pullRequest/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pullRequest/merge").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pullRequest/reassign").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": {\"code\": \"UNAUTHORIZED\", \"message\": \"Invalid or missing token\"}}");
                        })
                );

        return http.build();
    }
}
