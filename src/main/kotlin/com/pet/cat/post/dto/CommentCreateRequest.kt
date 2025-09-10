package com.pet.cat.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentCreateRequest(
    @field:NotBlank(message = "본문은 비어있을 수 없습니다.")
    @field:Size(min = 1, max = 1000, message = "댓글은 1자 이상 1000자 이하여야 합니다.")
    val body: String,
    val parentId: Long? = null
)