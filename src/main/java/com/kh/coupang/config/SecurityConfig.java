package com.kh.coupang.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration // 설정 파일이라는 annotation
@EnableWebSecurity
@RequiredArgsConstructor // 생성자
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 특정 http 요청에 대한 웹 기반 보안 구성. 인증/인가 및 로그아웃
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 시큐리티 6.2.1 버전
        // 시큐리티가 제공하는 것들을 막음

        /* http.csrf(csrf -> csrf.disable())
        .httpBasic(basic -> basic.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/signUp").permitAll()
                 .anyRequest().authenticated())
        .build();
        */

        // 위조 요청 막음
        http.csrf(csrf -> csrf.disable());
        // 기본으로 제공하고 있는 보안
        http.httpBasic(basic -> basic.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // "" 안의 모든 경로에 허용시키겠다
        http.authorizeHttpRequests(authorize ->
                authorize
                        // 회원가입만 허용 -> 기존에 되던 전체보기 등등도 안됨
                .requestMatchers("/signUp", "/login").permitAll()
                        // hasRole("USER") -> USER만 가능하도록 (SpringSecurity가 ROLE_를 자동으로 인식
                        .requestMatchers("/api/product").hasAnyRole("USER")
                        .anyRequest().authenticated())
                .addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);
        return http.build();
    }
}
