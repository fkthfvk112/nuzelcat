package com.pet.cat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CatApplication

fun main(args: Array<String>) {
	runApplication<CatApplication>(*args)
}
