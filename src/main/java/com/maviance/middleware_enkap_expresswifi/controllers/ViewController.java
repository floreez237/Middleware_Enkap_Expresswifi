package com.maviance.middleware_enkap_expresswifi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/test")
    public String redirect() {
        return "redirect:https://www.google.com";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
