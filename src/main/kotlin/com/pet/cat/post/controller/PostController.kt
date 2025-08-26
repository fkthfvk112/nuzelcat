package com.pet.cat.post.controller

import com.pet.cat.post.dto.ImageCardDTO
import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.service.Interface.IPostService
import com.pet.cat.post.service.PostService
import com.pet.cat.utils.adivce.LoggingAdvice
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.web.CurrentVisitor
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/post")
@RestController
class PostController(
    val postService: IPostService
) {

    /* 저장 */
    @PostMapping
    fun savePost(
        @Valid @RequestBody post:PostCreateRequest,
        @CurrentVisitor visitor: CurrentVisitorDto
    ): ResponseEntity<Long> {
        return LoggingAdvice.infolog("controller-saveAllGoal"){
            val userId = SecurityContextHolder.getContext().authentication.name

            ResponseEntity.ok(postService.createPost(post, visitor).data)
        }
    }

    /* 단건 조회 */
    @GetMapping("/{postId}")
    fun getPostDetail(
        @PathVariable postId: Long,
        @CurrentVisitor visitor: CurrentVisitorDto
    ): ResponseEntity<PostDetailDto>{
        return LoggingAdvice.infolog("controller-getPostDetail"){
            ResponseEntity.ok(postService.getPostDetail(postId, visitor))
        }
    }

    @GetMapping("/list")
    fun getPostList(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) catName: String?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(defaultValue = "score_desc") sortDir: String,
        pageable: Pageable
    ): ResponseEntity<Page<ImageCardDTO>> {
        return LoggingAdvice.infolog("controller-getPostList"){
            ResponseEntity.ok(
                postService.getImageCardList(title, catName, tag, sortDir, pageable)
            )
        }
    }

    /* 좋아요 (하루 1회만 가능) */
    @PostMapping("/{postId}/like")
    fun addLike(
        @PathVariable postId: Long,
        @CurrentVisitor visitor: CurrentVisitorDto
    ): ResponseEntity<Long?> {
        return LoggingAdvice.infolog("controller-addLike") {
            ResponseEntity.ok(postService.addLikeCnt(postId, visitor).data)
        }
    }
}