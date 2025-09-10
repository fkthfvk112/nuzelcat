package com.pet.cat.post.service.Interface

import com.pet.cat.post.dto.CommentDTO
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto

interface ICommentService {
    fun createComment(postId: Long, body: String, parentId: Long? = null, visitor: CurrentVisitorDto): CommonResult<CRUDStateEnum, CommentDTO>
    fun getComments(postId: Long): CommonResult<CRUDStateEnum, List<CommentDTO>>
}