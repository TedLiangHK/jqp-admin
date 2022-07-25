package com.jqp.admin.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private AdminInterceptor adminInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(adminInterceptor);
        interceptorRegistration.addPathPatterns("/**");
        interceptorRegistration.excludePathPatterns(
                "/admin/css/**",
                "/admin/js/**",
                "/admin/images/**",
                "/admin/fonts/**",
                "/admin/lyear_pages_login.html",
                "/admin/choose_enterprise.html",
                "/captcha.png",
                "/admin/user/login",
                "/admin/user/login/*",
                "/admin/user/logout",
                "/amis/**",
                "/amis-editor/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600);
    }
}
