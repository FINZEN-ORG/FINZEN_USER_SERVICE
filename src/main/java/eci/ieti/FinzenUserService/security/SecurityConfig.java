package eci.ieti.FinzenUserService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilita CSRF para simplificar en APIs REST
                //.csrf(csrf -> csrf.disable())
                // Configuración de autorización
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Deshabilita formulario de login por defecto
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable());
        return http.build();
    }
}