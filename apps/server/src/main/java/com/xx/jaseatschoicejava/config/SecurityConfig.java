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
            // 公开接口：登录 / 注册 / 验证码。清单与前端 PUBLIC_API_PATHS、
            // 小程序 AUTH_WHITELIST 对齐（见 docs/technical/public-routes.md）。
            .requestMatchers(
                "/v1/users/login",
                "/v1/users/register",
                "/v1/users/send-sms-code",
                "/v1/users/send-email-code",
                "/v1/users/wechat-login",
                "/v1/users/reset-password",
                "/v1/merchant/login",
                "/v1/merchant/register",
                "/admin/login",
                "/v1/captcha/**"
            ).permitAll()
            // 运维与联调：compose 健康检查、wait-healthy.sh、check_routes.py 依赖。
            .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/doc.html",
                "/webjars/**"
            ).permitAll()
            .requestMatchers("/error").permitAll()
            // 图片直出：<img> 标签无法携带 Authorization，头像/菜品/聊天图片只能匿名读取。
            //   1. /uploads/**、/files/**  运行时上传目录（WebConfig 资源映射 + FileController/FileAccessController 读取）
            //   2. /synthetic-assets/**   入库的合成素材（V15 把演示账号头像迁到此处）
            // 只放这两类「按目录读」的路径；根级 /*.png 兼容入口（FileAccessController
            // 的 /{filename:.+}）不放行——它等价于按文件名匿名遍历上传目录，
            // 超出最小公开集，需要时再单独登记。
            .requestMatchers("/uploads/**", "/files/**", "/synthetic-assets/**").permitAll()
            // 食谱只读：小程序「食谱」是 tabBar 页（pages/recipe/index），未登录可达，
            // 进页即拉今日推荐；点击「一键订餐」时才在小程序侧要求登录。
            // 只放行读（GET），写入 /v1/recipe（POST 新建）仍要求登录。
            .requestMatchers(HttpMethod.GET, "/v1/recipe/**").permitAll()
            // 其余一律要求登录：新增接口忘记加注解时返回 401，而不是匿名可调。
            .anyRequest().authenticated();

        return http.build();
    }
}