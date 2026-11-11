package com.xx.jaseatschoicejava.config;

import com.xx.jaseatschoicejava.filter.JwtAuthenticationFilter;
import com.xx.jaseatschoicejava.security.SecurityAccessDeniedHandler;
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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityAccessDeniedHandler securityAccessDeniedHandler;

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
            System.out.println("Content-Type: " + httpRequest.getContentType());
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
            // 未登录访问受保护接口时返回 401，交由前端既有流程跳登录
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期，请重新登录\"}");
                })
                .accessDeniedHandler(securityAccessDeniedHandler))
            // Configure authorization (Spring Security 6: authorizeHttpRequests + requestMatchers)
            .authorizeHttpRequests()
            // ASYNC 是同一请求的续派发（SSE 结束时容器会再走一遍过滤器链）。
            // 首次 REQUEST 派发已经完成鉴权，这里放行以免 SseEmitter 完成时被二次拦截，
            // 导致响应已提交却抛 AccessDenied，chunked 响应收不到终止块。
            .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow all OPTIONS requests
            .requestMatchers("/v1/content-extraction/**").authenticated() // 内容提取一律要求登录
            .requestMatchers("/v1/feedback/**").authenticated() // 用户反馈一律要求登录（归属取自可信 JWT）
            .requestMatchers(
                "/v1/ai/dish-recognize",
                "/v1/ai/dish-recognize-url",
                "/v1/ai/recipe",
                "/v1/ai/recipe/stream",
                "/v1/ai/recipe-upload"
            ).authenticated() // AI 饮食助手禁止匿名消耗模型额度
            .requestMatchers("/**").permitAll()
            .anyRequest().permitAll();

        return http.build();
    }
}