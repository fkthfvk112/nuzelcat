package com.pet.cat.post.controller

import com.pet.cat.post.dto.ImageCardDTO
import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.service.Interface.IPostService
import com.pet.cat.post.service.PostService
import com.pet.cat.utils.adivce.LoggingAdvice
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.web.CurrentVisitor
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

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

    //(허용: asc | desc | score_asc | score_desc)
    @GetMapping("/list")
    fun getPostList(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) catName: String?,
        @RequestParam(required = false) author: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(defaultValue = "score_desc") sortDir: String,
        @RequestParam(required = false) exceptPostId:String?,
        @RequestParam(required = false) fromYMD:String?,
        @RequestParam(required = false) toYMD:String?,
        pageable: Pageable
    ): ResponseEntity<Page<ImageCardDTO>> {
        return LoggingAdvice.infolog("controller-getPostList"){
            ResponseEntity.ok(
                postService.getImageCardList(title, catName, author, tags, sortDir, exceptPostId, fromYMD, toYMD, pageable)
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

    @DeleteMapping("/{postId}")
    fun deletePost(
        @PathVariable postId: Long,
        @RequestBody req: Map<String, String>
    ): ResponseEntity<CRUDStateEnum> {
        return LoggingAdvice.infolog("controller-deletePost") {
            val pw = req["pw"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 필요합니다.")

            ResponseEntity.ok(postService.deletePost(postId, pw).state)
        }
    }
}