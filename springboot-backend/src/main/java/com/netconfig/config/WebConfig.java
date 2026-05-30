package com.netconfig.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${project.root:./}")
    private String projectRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absPath = new File(projectRoot).getAbsolutePath();
        registry.addResourceHandler("/**")
                .addResourceLocations("file:" + absPath + "/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/pages/cmd/cmd_list.html");
    }
}
