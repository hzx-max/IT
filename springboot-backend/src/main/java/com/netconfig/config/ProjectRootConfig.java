package com.netconfig.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ProjectRootConfig {

    @Bean
    public String projectRoot() {
        String root = System.getProperty("project.root");
        if (root == null || root.isEmpty()) {
            root = new File(".").getAbsolutePath();
            if (root.endsWith(".")) root = root.substring(0, root.length() - 1);
            if (root.endsWith(File.separator)) root = root.substring(0, root.length() - 1);
        }
        return root;
    }
}
