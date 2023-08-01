package com.example.config;

import com.example.config.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  09:50
 * @Description: jwt拦截器
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;
    @Value("${global.file-path-window}")
    private String filePathWindow;
    @Value("${global.file-path-linux}")
    private String filePathLinux;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")    //拦截所有请求 通过判断token是否合法来决定是否登陆
                .excludePathPatterns("/user/login","/user/qqlogin","/user/register",
                        "/user/check/**","/doc.html/**",
                        "/doc.*",
                        "/swagger-ui.*",
                        "/swagger-resources",
                        "/webjars/**",
                        "/v2/api-docs/**",
                        "/v3/api-docs",
                        "/**/export","/**/import", "/file/**","/role/**","/menu/**",
                        "/**/*.js", "/**/*.png", "/**/*.jpg",
                        "/**/*.jpeg", "/**/*.gif"); //放行接口
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name");

        //如果是Windows系统
        if (os.toLowerCase().startsWith("win")) {
            registry.addResourceHandler("/pictures/**")
                    .addResourceLocations(filePathWindow);
        } else {  //linux 和mac
            registry.addResourceHandler("/pictures/**")
                    .addResourceLocations(filePathLinux) ;
        }

        //配置拦截器访问静态资源
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//        registry.addResourceHandler("/pictures/**")
//                .addResourceLocations("file:"+"/pictures/");

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOrigins("*");//允许域名访问，如果*，代表所有域名
    }



}
