package com.pet.cat.seo.controller

import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.seo.service.ISeoService.ISeoService
import com.pet.cat.utils.adivce.LoggingAdvice
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.web.CurrentVisitor
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/seo")
@RestController
class SEOController(
    val seoService:ISeoService
) {
    @GetMapping("/sitemap")
    fun generateSitemap(): ResponseEntity<String> {
        return LoggingAdvice.infolog("controller-saveAllGoal"){
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(seoService.getSiteMap());
        }
    }
}