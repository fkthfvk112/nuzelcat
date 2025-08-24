package com.pet.cat.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AppConfig(
    @Value("\${cloudinary.cloudName}")
    private val cloudName: String,

    @Value("\${cloudinary.apiKey}")
    private val apiKey: String,

    @Value("\${cloudinary.apiSecret}")
    private val apiSecret: String,
) {

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
}