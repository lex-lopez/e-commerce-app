package com.alopez.store.admin.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @RequestMapping("/hello")
    public String sayHello() {
        return "Hello from AdminController";
    }
}
