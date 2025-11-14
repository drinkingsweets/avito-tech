package org.example.avitotech.config;

import jakarta.servlet.http.HttpServletResponse;
import org.example.avitotech.jwt.JwtAuthenticationFilter;
import org.example.avitotech.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/auth/admin-token", "/auth/user-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/team/add").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/team/get").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/users/setIsActive").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/getReview").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/pullRequest/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pullRequest/merge").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pullRequest/reassign").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .logout(logout -> logout.disable())
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
