package com.smarthfashion.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    private final Environment env;

    public SecurityConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User
                .withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean allowPublicAdmin = Boolean.parseBoolean(env.getProperty("ADMIN_ALLOW_PUBLIC", "false"));

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher("/api/internal/**"),
                        new AntPathRequestMatcher("/login"),
                        new AntPathRequestMatcher("/logout")
                )
            )
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/login", "/admin/login", "/error", "/sso/login", "/ping", "/tracking/**",
                        "/api/internal/**"
                ).permitAll();

                if (allowPublicAdmin) {
                    auth.requestMatchers("/admin/**").permitAll();
                }

                auth.anyRequest().authenticated();
            })
            .formLogin(login -> login
                .loginPage("/admin/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/products", true)
                .permitAll()
            )
            // Habilitar autenticación básica para pruebas (curl) además del formulario
            .httpBasic(customizer -> { })
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
