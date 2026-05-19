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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // ===== Публичное =====
                        .requestMatchers("/", "/index.html", "/*.js", "/*.css", "/*.ico",
                                "/assets/**", "/media/**").permitAll()
                        .requestMatchers("/users/register").permitAll()
                        .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ===== Управление пользователями — только ADMIN =====
                        .requestMatchers(HttpMethod.GET,    "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                        // ===== Сотрудники — справочник, ведёт только ADMIN =====
                        .requestMatchers(HttpMethod.POST,   "/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/employees/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/employees/**").hasRole("ADMIN")

                        // ===== Типы оборудования — только ADMIN =====
                        .requestMatchers(HttpMethod.POST,   "/types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/types/**").hasRole("ADMIN")

                        // ===== Оборудование — write для ENGINEER и ADMIN =====
                        .requestMatchers(HttpMethod.POST,   "/equipments/**").hasAnyRole("ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/equipments/**").hasAnyRole("ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/equipments/**").hasAnyRole("ENGINEER", "ADMIN")

                        // ===== Порты — write для ENGINEER и ADMIN =====
                        .requestMatchers(HttpMethod.POST,   "/ports/**").hasAnyRole("ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/ports/**").hasAnyRole("ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ports/**").hasAnyRole("ENGINEER", "ADMIN")

                        // ===== IP-адреса — write для ENGINEER и ADMIN =====
                        .requestMatchers(HttpMethod.POST,   "/ip-addresses/**").hasAnyRole("ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/ip-addresses/**").hasAnyRole("ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ip-addresses/**").hasAnyRole("ENGINEER", "ADMIN")

                        // ===== История обслуживания =====
                        // TECHNIC, ENGINEER, ADMIN могут создавать и редактировать ТО
                        .requestMatchers(HttpMethod.POST,   "/histoires/**").hasAnyRole("TECHNIC", "ENGINEER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/histoires/**").hasAnyRole("TECHNIC", "ENGINEER", "ADMIN")
                        // Удалять записи ТО — только ENGINEER и ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/histoires/**").hasAnyRole("ENGINEER", "ADMIN")

                        // ===== Сканирование сети — ENGINEER и ADMIN =====
                        .requestMatchers("/discovery/**").hasAnyRole("ENGINEER", "ADMIN")

                        // Всё остальное — для авторизованных (это в основном GET-запросы)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
