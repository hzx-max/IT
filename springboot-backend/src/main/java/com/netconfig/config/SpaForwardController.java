package com.netconfig.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping(value = {"/", "/cmd", "/cmd/**", "/fault", "/fault/**",
            "/desktop", "/desktop/**", "/linux", "/linux/**",
            "/office", "/office/**", "/ai", "/ai/**", "/notes", "/notes/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
