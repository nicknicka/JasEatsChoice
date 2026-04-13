package com.xx.jaseatschoicejava.config;

import com.xx.jaseatschoicejava.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 允许URL中包含双斜杠的防火墙
    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowSemicolon(true);
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    // 请求日志过滤器，用于调试
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // 确保在最前面
        return registration;
    }

    // 自定义请求日志过滤器
    public static class RequestLoggingFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            System.out.println("========================================");
            System.out.println("请求URL: " + httpRequest.getRequestURL());
            System.out.println("请求URI: " + httpRequest.getRequestURI());
            System.out.println("ContextPath: " + httpRequest.getContextPath());
            System.out.println("ServletPath: " + httpRequest.getServletPath());
            System.out.println("QueryString: " + httpRequest.getQueryString());
            System.out.println("Method: " + httpRequest.getMethod());
            System.out.println("========================================");
            chain.doFilter(request, response);
        }

        @Override
        public void init(FilterConfig filterConfig) {}

        @Override
        public void destroy() {}
    }

    // Configure PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection for API endpoints
            .csrf().disable()
            // 禁用 Session 创建（解决流式响应警告）
            .sessionManagement()
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
            .and()
            // Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.access.intercept.FilterSecurityInterceptor.class)
            // Configure authorization (Spring Security 6: authorizeHttpRequests + requestMatchers)
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow all OPTIONS requests
            .requestMatchers("/**").permitAll()
            .anyRequest().permitAll();

        return http.build();
    }
}