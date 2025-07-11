package com.example.devopsapp.security;

import com.example.devopsapp.model.User;
import com.example.devopsapp.model.Admin;
import com.example.devopsapp.repository.UserRepository;
import com.example.devopsapp.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // First check if it's a regular user
            User user = userRepository.findByEmail(username).orElse(null);
            if (user != null) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles("USER")
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
            }

            // Then check if it's an admin
            Admin admin = adminRepository.findByEmail(username).orElse(null);
            if (admin != null && admin.isActive()) {
                return org.springframework.security.core.userdetails.User.builder()
                        .username(admin.getEmail())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build();
            }

            throw new UsernameNotFoundException("User not found with email: " + username);
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased strength
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = "/dashboard"; // Default for users
            
            // Check if user has ADMIN role
            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                redirectUrl = "/admin/dashboard";
            }
            
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection (enabled for forms, disabled for API endpoints)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/tasks/**", "/admin/api/**", "/admin/users/**", "/admin/tasks/**") // API endpoints
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // Security headers
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            )
            
            // Session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(5) // Allow max 5 concurrent sessions per user
                .maxSessionsPreventsLogin(false)
                .and()
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/login?expired")
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/register", "/css/**", "/js/**", "/images/**",
                    "/actuator/health", "/actuator/health/**",
                    "/error", "/favicon.ico"
                ).permitAll()
                
                // Admin endpoints
                .requestMatchers("/admin/register", "/admin/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // User endpoints
                .requestMatchers("/tasks/**").hasRole("USER")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            // Form login configuration
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username") // This will be email
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Remember me functionality
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService())
            );
            
        return http.build();
    }
}
