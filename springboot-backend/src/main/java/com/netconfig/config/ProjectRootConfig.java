package com.netconfig.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ProjectRootConfig {

    @Bean
    public String projectRoot() {
        String root = System.getProperty("project.root");
        if (root == null || root.isEmpty()) {
            File dir = new File(".").getAbsoluteFile();
            if (dir.getName().equals("springboot-backend")) {
                dir = dir.getParentFile();
            }
            root = dir.getAbsolutePath();
        }
        return root;
    }
}
