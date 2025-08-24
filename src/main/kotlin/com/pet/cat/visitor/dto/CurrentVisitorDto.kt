package com.pet.cat.visitor.dto

import jakarta.persistence.Column

data class CurrentVisitorDto(
    val id: String?,
    val remoteIp: String? = null,
    val userAgent: String? = null,
    val acceptLanguage: String? = null,
    val tzOffsetMinutes: Int? = null,
)