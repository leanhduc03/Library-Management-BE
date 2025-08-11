package com.spring.LibraryManagement.Config;

import com.spring.LibraryManagement.Filter.JwtAuthenticationFilter;
import com.spring.LibraryManagement.Security.CustomPermissionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomPermissionEvaluator customPermissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        log.info("Created AuthenticationProvider");
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        log.info("Creating authentication Manager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/auth/**").permitAll()

                        //Books API
                        .requestMatchers(HttpMethod.GET, "/books/**").hasAuthority("PERMISSION_BOOK_READ")
                        .requestMatchers(HttpMethod.POST, "/books/**").hasAuthority("PERMISSION_BOOK_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/books/**").hasAuthority("PERMISSION_BOOK_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/books/**").hasAuthority("PERMISSION_BOOK_DELETE")

                        //Borrow API
                        .requestMatchers(HttpMethod.POST, "/borrows").hasAuthority("PERMISSION_BORROW_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/borrows/*/return").hasAuthority("PERMISSION_BORROW_UPDATE")
                        .requestMatchers(HttpMethod.GET, "/borrows").hasAuthority("PERMISSION_BORROW_READ")
                        .requestMatchers(HttpMethod.GET, "/borrows/*").hasAuthority("PERMISSION_BORROW_READ")
                        .requestMatchers(HttpMethod.GET, "/borrows/overdue").hasRole("ADMIN")

                        //Fine API
                        .requestMatchers(HttpMethod.GET, "/fines/my-fines").authenticated()
                        .requestMatchers(HttpMethod.GET, "/fines/my-total").authenticated()
                        .requestMatchers(HttpMethod.GET, "/fines").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/fines/user/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/fines/*/mark-paid").hasRole("ADMIN")

                        //File Upload API
                        .requestMatchers(HttpMethod.POST, "/api/upload/image").hasAnyAuthority(
                                "PERMISSION_BOOK_CREATE", "PERMISSION_BOOK_UPDATE")

                        //Admin API
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        //User API
                        .requestMatchers("/user/**").hasRole("USER")
                        
                        // Tất cả các request còn lại cần xác thực
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/access-denied"))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());
        SecurityFilterChain https = http.build();
        log.info("https: {}", https);
        return https;
    }
}
