package com.pet.cat.post.dto
import java.time.LocalDateTime
data class ImageCardDTO(
    val postId: Long,
    val repreImgUrl: String,
    val imgCnt: Long,
    val createdAt: LocalDateTime?,
    val title: String?,
    val catName: String,
    val tags: List<String>,
    val likeCnt: Long,
    val viewCnt: Long
)
