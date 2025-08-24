package com.pet.cat.test.controller

import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
class TestController {
    @GetMapping("/hello")
    fun helloWorld(): String {
        return "Hello World"
    }
}