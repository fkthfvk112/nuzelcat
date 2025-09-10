package com.pet.cat.post.dto
import java.time.LocalDateTime

data class CommentDTO (
    val id: Long,
    val postId: Long,
    val parentId: Long?,
    val body: String,
    val isDel: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val children: List<CommentDTO> = emptyList()
)