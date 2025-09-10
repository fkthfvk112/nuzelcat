package com.pet.cat.post.controller

import com.pet.cat.post.dto.CommentCreateRequest
import com.pet.cat.post.dto.CommentDTO
import com.pet.cat.post.service.Interface.ICommentService
import com.pet.cat.utils.adivce.LoggingAdvice
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.web.CurrentVisitor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/comment")
@RestController
class CommentController(
    val commentService: ICommentService
) {
    /** 댓글/대댓글 작성 */
    @PostMapping("/{postId}")
    fun createComment(
        @PathVariable postId: Long,
        @RequestBody req: CommentCreateRequest,
        @CurrentVisitor visitor: CurrentVisitorDto
    ): ResponseEntity<CRUDStateEnum> {
        return LoggingAdvice.infolog("controller-createComment") {
            ResponseEntity.ok(
                commentService.createComment(
                    postId = postId,
                    body = req.body,
                    parentId = req.parentId,
                    visitor = visitor
                ).state
            )
        }
    }

    /** 댓글 트리 조회 (깊이 2: 댓글 + 대댓글) */
    @GetMapping("/{postId}")
    fun getComments(
        @PathVariable postId: Long
    ): ResponseEntity<List<CommentDTO>?> {
        return LoggingAdvice.infolog("controller-getComments") {
            ResponseEntity.ok(
                commentService.getComments(postId).data
            )
        }
    }
}