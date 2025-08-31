package com.pet.cat.post.dto

import java.time.LocalDateTime

data class PostDetailDto(
    val postId: Long,
    val images: List<String>,
    val title: String?,
    val catName: String?,
    val author: String?,
    val description: String?,
    val tags: List<String>,
    val likeCnt: Long,
    val viewCnt: Long,
    val createdAt: LocalDateTime?
)