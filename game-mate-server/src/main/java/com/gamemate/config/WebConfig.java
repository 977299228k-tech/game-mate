package com.gamemate.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Configuration
public class WebConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("【WebConfig】创建 PasswordEncoder Bean");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        log.info("【WebConfig】创建 RestTemplate Bean");
        return new RestTemplate();
    }

    @Bean
    public OncePerRequestFilter requestLoggingFilter() {
        log.info("【WebConfig】创建请求日志过滤器");
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String origin = request.getHeader("Origin");

                log.info("【RequestFilter】收到请求: {} {} | Origin: {} | Content-Type: {}",
                         method, uri, origin, request.getContentType());

                if ("OPTIONS".equals(method)) {
                    log.info("【RequestFilter】OPTIONS 预检请求，设置 CORS 头并直接返回");
                    response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
                    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    response.setHeader("Access-Control-Allow-Headers", "*");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }

                if (origin != null) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                    response.setHeader("Access-Control-Allow-Headers", "*");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                }

                log.info("【RequestFilter】放行请求: {} {}", method, uri);
                filterChain.doFilter(request, response);
            }
        };
    }
}
