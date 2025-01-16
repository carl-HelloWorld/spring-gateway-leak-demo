package com.hello.controller;


import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
	
    @RequestMapping("/hello")
    public String index(@RequestParam String name) {
        return "hello "+name+"，this is first messge";
    }

    @PostMapping("/hello")
    public String index1(@RequestBody String name) {
        return "hello "+name+"，this is first messge";
    }
}