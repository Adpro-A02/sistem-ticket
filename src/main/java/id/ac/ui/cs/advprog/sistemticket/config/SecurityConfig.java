package id.ac.ui.cs.advprog.sistemticket.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import id.ac.ui.cs.advprog.sistemticket.security.JwtAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed.origin:http://localhost:3000}")
    private String allowedOrigin;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Read operations - permitted for all users (Guest, User, Admin, Organizer)
                        .requestMatchers("/api/tickets").permitAll()
                        .requestMatchers("/api/tickets/available").permitAll()
                        .requestMatchers("/api/tickets/event/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/**").permitAll()
                        
                        // Create operations - only for Organizer
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("ORGANIZER")
                        .requestMatchers("/api/tickets/batch").hasRole("ORGANIZER")
                        
                        // Delete operations - for Admin and Organizer
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasAnyRole("ADMIN", "ORGANIZER")
                        
                        // Purchase operations - only for authenticated users (User)
                        .requestMatchers("/api/tickets/*/purchase").hasAnyRole("ATTENDEE", "ADMIN")
                        
                        // Validate operations - for Admin and Organizer
                        .requestMatchers("/api/tickets/*/validate").hasAnyRole("ORGANIZER", "ADMIN")
                        
                        // Status update - only for Admin
                        .requestMatchers("/api/tickets/*/status").hasAnyRole("ADMIN", "ORGANIZER")
                        
                        // Default for other endpoints
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigin));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
