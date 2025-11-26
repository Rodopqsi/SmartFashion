package com.smarthfashion.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.core.env.Environment;
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
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User
                .withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean allowPublicAdmin = Boolean.parseBoolean(env.getProperty("ADMIN_ALLOW_PUBLIC", "false"));

        http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/css/**", "/admin/login", "/error", "/sso/login", "/ping", "/tracking/**").permitAll();
                auth.requestMatchers("/api/internal/**").permitAll();
                if (allowPublicAdmin) {
                    // Temporary debug mode: allow public access to admin UI
                    auth.requestMatchers("/admin/**").permitAll();
                } else {
                    auth.requestMatchers("/admin/**").hasRole("ADMIN");
                }
                auth.anyRequest().authenticated();
            })

            .formLogin(login -> login
                .loginPage("/admin/login")
                .defaultSuccessUrl("/admin/products", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/api/internal/**")));
        return http.build();
    }
}
