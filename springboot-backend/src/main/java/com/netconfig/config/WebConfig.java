package com.netconfig.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private String getVueDistPath() {
        File dir = new File(System.getProperty("user.dir")).getAbsoluteFile();
        if (dir.getName().equals("springboot-backend") || dir.getName().equals("target")) {
            dir = dir.getParentFile();
        }
        return new File(dir, "vue-frontend/dist").getAbsolutePath().replace("\\", "/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String vueDistPath = getVueDistPath();
        String resourceLocation = "file:" + vueDistPath + "/";

        registry.addResourceHandler("/static/**")
                .addResourceLocations(resourceLocation + "static/");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations(resourceLocation + "assets/");

        registry.addResourceHandler("/*.html")
                .addResourceLocations(resourceLocation);

        registry.addResourceHandler("/*.ico")
                .addResourceLocations(resourceLocation);

        registry.addResourceHandler("/*.js")
                .addResourceLocations(resourceLocation);

        registry.addResourceHandler("/*.css")
                .addResourceLocations(resourceLocation);

        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) uploadDir.mkdirs();
        String uploadPath = uploadDir.getAbsolutePath().replace("\\", "/");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
