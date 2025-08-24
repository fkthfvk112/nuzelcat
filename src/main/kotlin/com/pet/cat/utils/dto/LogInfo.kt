package com.pet.cat.utils.dto

import java.time.LocalDateTime

data class LogInfo(
    val level:String,
    val funcName:String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)
