package com.pet.cat.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.pet.cat.visitor.web.CurrentVisitorArgumentResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class AppConfig(
    @Value("\${cloudinary.cloudName}")
    private val cloudName: String,

    @Value("\${cloudinary.apiKey}")
    private val apiKey: String,

    @Value("\${cloudinary.apiSecret}")
    private val apiSecret: String,

    private val currentVisitorArgumentResolver: CurrentVisitorArgumentResolver
) : WebMvcConfigurer {   // ← 여기 추가

    @Bean
    fun cloudinary(): Cloudinary {
        return Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
            )
        )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentVisitorArgumentResolver)
    }
}
