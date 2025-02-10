package com.balki.twitter_clone.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationProvider authenticationProvider;

    private final String baseUrl = "/api/1.0";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher(baseUrl + "/users/logout")).authenticated()
                .requestMatchers(new AntPathRequestMatcher(baseUrl + "/users/refresh-token")).authenticated()
                .requestMatchers(new AntPathRequestMatcher(baseUrl + "/users/update/{id}")).authenticated()
                .requestMatchers(new AntPathRequestMatcher(baseUrl + "/users/delete/{id}")).authenticated()
                /*

                        .requestMatchers(new AntPathRequestMatcher("/api/1.0/twitters/save")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/1.0/file/upload")).authenticated()*/
                .anyRequest()
                .permitAll()
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers().frameOptions().sameOrigin();

        return http.build();
    }
}