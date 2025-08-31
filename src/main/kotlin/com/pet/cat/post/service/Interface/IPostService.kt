package com.pet.cat.post.service.Interface

import com.pet.cat.post.dto.ImageCardDTO
import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.entity.PostEntity
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IPostService {
    fun createPost(request: PostCreateRequest, visitor: CurrentVisitorDto?): CommonResult<CRUDStateEnum, Long?>
    fun getPostDetail(postId: Long, visitor: CurrentVisitorDto?): PostDetailDto
    fun getImageCardList(
        title: String?,
        catName: String?,
        tags: List<String>?,
        sortDir: String,
        exceptPostId:String?,
        fromYMD:String?,
        toYMD:String?,
        pageable: Pageable
    ): Page<ImageCardDTO>

    fun addViewCnt(postId:Long, visitor:CurrentVisitorDto): CommonResult<CRUDStateEnum, Long?>
    fun addLikeCnt(postId: Long, visitor: CurrentVisitorDto): CommonResult<CRUDStateEnum, Long?>
    fun deletePost(postId:Long, pw:String):CommonResult<CRUDStateEnum, Unit>
}