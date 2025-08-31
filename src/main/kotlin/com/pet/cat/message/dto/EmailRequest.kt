package com.pet.cat.message.dto

data class EmailRequest (
    val emailAddress:String,
    val emailTitle:String,
    val emailContent:String
)