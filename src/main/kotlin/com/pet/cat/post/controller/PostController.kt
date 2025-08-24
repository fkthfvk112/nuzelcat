package com.pet.cat.post.controller

import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.service.Interface.IPostService
import com.pet.cat.post.service.PostService
import com.pet.cat.utils.adivce.LoggingAdvice
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.web.CurrentVisitor
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
    fun getPostDetail(@PathVariable postId: Long): ResponseEntity<PostDetailDto>{
        return LoggingAdvice.infolog("controller-getPostDetail"){
            ResponseEntity.ok(postService.getPostDetail(postId))
        }
    }

}