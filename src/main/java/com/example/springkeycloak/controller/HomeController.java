package com.example.springkeycloak.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(path = "/home")
    @PreAuthorize("hasAnyRole('ADMIN-REALM')")
    public String home(){
        return "home";
    }
}
