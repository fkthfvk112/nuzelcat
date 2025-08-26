// src/main/kotlin/com/pet/cat/config/SecurityConfig.kt
package com.pet.cat.config

import com.pet.cat.visitor.web.VisitorIdentificationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val visitorFilter: VisitorIdentificationFilter,
    @Value("\${allowed_url_csv_list:}") // 기본값 "" 주입
    private val allowedUrlCsvList: String
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // API 서버라면 CSRF 비활성화
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .addFilterAfter(visitorFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val allowedOriginList = allowedUrlCsvList.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val corsConfiguration = CorsConfiguration().apply {
            allowedOrigins = allowedOriginList
            allowedHeaders = listOf("*")
            allowedMethods = listOf("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfiguration)
        }
    }
}
