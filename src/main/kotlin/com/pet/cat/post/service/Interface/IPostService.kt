package com.pet.cat.post.service.Interface

import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.entity.PostEntity
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity

interface IPostService {
    fun createPost(request: PostCreateRequest, visitor: CurrentVisitorDto?): CommonResult<CRUDStateEnum, Long?>
    fun getPostDetail(postId: Long): PostDetailDto
}