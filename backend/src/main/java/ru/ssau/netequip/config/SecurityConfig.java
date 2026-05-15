package ru.ssau.netequip.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.ssau.netequip.security.JwtFilter;
import ru.ssau.netequip.service.CustomUserDetailsService;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource(){
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:4200"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//        return new UrlBasedCorsConfigurationSource(){{
//            registerCorsConfiguration("/**", config);
//        }};
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Статические файлы фронта — без авторизации
                        .requestMatchers("/", "/index.html", "/*.js", "/*.css", "/*.ico",
                                "/assets/**", "/media/**").permitAll()
                        // Регистрация и аутентификация — без входа
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/refresh").permitAll()
                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/employees/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/types/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/equipments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/equipments/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/ip-addresses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/ip-addresses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ip-addresses/**").hasRole("ADMIN")

                        // Порты — DELETE только ADMIN, POST/PUT доступны всем (соединения)
                        .requestMatchers(HttpMethod.DELETE, "/ports/**").hasRole("ADMIN")

                        // История обслуживания — DELETE только ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/histoires/**").hasRole("ADMIN")

                        // Все остальные — для авторизованных пользователей
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
