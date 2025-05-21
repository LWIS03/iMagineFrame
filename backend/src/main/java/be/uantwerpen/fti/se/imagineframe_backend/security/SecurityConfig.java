package be.uantwerpen.fti.se.imagineframe_backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${frontend_url}")
    private String frontendUrl;

    private final JWTFilter filter;
    private final SEUserDetailsService userDetailsService;

    public SecurityConfig(JWTFilter filter, SEUserDetailsService userDetailsService) {
        this.filter = filter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .headers((headers) -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .cors((cors) -> cors
                        .configurationSource(request -> {
                                    var corsConf = new CorsConfiguration();
                                    corsConf.setAllowedOrigins(List.of(frontendUrl));
                                    corsConf.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                                    corsConf.setAllowedHeaders(List.of("*"));
                                    return corsConf;
                                }
                        )
                )
                .authorizeHttpRequests(requests -> {
                    requests.requestMatchers("/auth/**").permitAll();
                    requests.requestMatchers("/users/register").permitAll();
                    requests.requestMatchers("/swagger-ui/**").permitAll();
                    requests.requestMatchers("/v3/api-docs/**").permitAll();
                    requests.requestMatchers("/h2-console/**").permitAll();
                    requests.requestMatchers("/register/new").permitAll();
                    requests.requestMatchers("/events/public").permitAll();
                    requests.requestMatchers("/projects/public").permitAll();
                    requests.requestMatchers("/events/*/public-participants").permitAll();
                    requests.requestMatchers("/events/*/public-participants/count").permitAll();
                    requests.requestMatchers("/projects/*/public-members").permitAll();
                    requests.requestMatchers("/projects/*/public-members/count").permitAll();
                    requests.requestMatchers("/api/files/report/**").permitAll();
                    requests.requestMatchers("/events/calendar/**").permitAll();
                    requests.requestMatchers("/error").anonymous(); // To allow error messages to be shown
                    requests.anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) ->
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage()))
                )
                .userDetailsService(userDetailsService)
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        //http.headers().frameOptions().sameOrigin();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
