package com.pet.cat.exception

data class ErrorResponse(
    val message: String,
    val status: Int,
    val error: String,
    val code: String
)