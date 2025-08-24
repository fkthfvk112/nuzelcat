package com.pet.cat.post.dto

import jakarta.validation.constraints.NotBlank

data class PostCreateRequest(
    @field:NotBlank
    val title: String? = null,

    @field:NotBlank
    val catName: String,

    @field:NotBlank
    val authorNickname: String,

    val description: String? = null,

    val pw:String?=null,

    @field:NotBlank
    val authorPassword: String,

    val tags:List<String>,

    val locationRegion: String? = null,

    val base64ImgList: List<String> = emptyList()
)

