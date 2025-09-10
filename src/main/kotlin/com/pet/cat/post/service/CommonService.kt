package com.pet.cat.post.service

import com.pet.cat.post.dto.CommentDTO
import com.pet.cat.post.entity.CommentEntity
import com.pet.cat.post.repository.CommentRepository
import com.pet.cat.post.repository.PostRepository
import com.pet.cat.post.service.Interface.ICommentService
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ICommentService {

    @Transactional
    override fun createComment(
        postId: Long,
        body: String,
        parentId: Long?,
        visitor: CurrentVisitorDto
    ): CommonResult<CRUDStateEnum, CommentDTO> {
        val post = postRepository.findById(postId).orElseThrow {
            EntityNotFoundException("존재하지 않는 게시글입니다. postId=$postId")
        }

        var validatedParentId: Long? = null

        if (parentId != null) {
            val parent = commentRepository.findByIdAndIsDelFalse(parentId)
                ?: throw IllegalArgumentException()

            if (parent.post?.id != postId) {
                throw IllegalArgumentException()
            }

            // 깊이 제한: 부모는 반드시 최상위 댓글이어야 함(= parent.parentId == null)
            if (parent.parentId != null) {
                throw IllegalArgumentException()
            }

            validatedParentId = parent.id
        }

        val entity = CommentEntity(
            visitor = visitor.id?.let { VisitorEntity(id = it) },
            post = post,
            parentId = validatedParentId,
            body = body,
        )

        val saved = commentRepository.save(entity)

        return CommonResult(
            state = CRUDStateEnum.CREATE_SUCCESS,
            message = "",
            data = saved.toDTO(children = emptyList())
        )
    }

    @Transactional(readOnly = true)
    override fun getComments(postId: Long): CommonResult<CRUDStateEnum, List<CommentDTO>> {
        // 부모 댓글 (정렬 기준은 기존 메서드 그대로 사용)
        val parents = commentRepository
            .findByPost_IdAndParentIdIsNullAndIsDelFalseOrderByCreatedAtDesc(postId)

        if (parents.isEmpty()) {
            return CommonResult(
                state = CRUDStateEnum.READ_SUCCESS,
                message = "",
                data = emptyList()
            )
        }

        // 부모 id 목록
        val parentIds = parents.mapNotNull { it.id }

        // 자식 댓글 일괄 조회 (삭제 제외는 레포 메서드에서 처리된다고 가정)
        val children = commentRepository.findChildrenByParentIds(postId, parentIds)

        // parentId -> children 매핑 (조회된 순서를 보존)
        val childrenMap: Map<Long, List<CommentEntity>> = children.groupBy { it.parentId!! }

        // [부모, 그 부모의 자식들..., 다음 부모, 그 부모의 자식들..., ...] 형태로 평탄화
        val flattened = mutableListOf<CommentDTO>()
        parents.forEach { parent ->
            // 1) 부모 먼저 추가 (children 비움)
            flattened += parent.toDTO(children = emptyList())

            // 2) 곧바로 해당 부모의 자식들 추가 (children 비움)
            val kids = childrenMap[parent.id] ?: emptyList()

            // 필요 시 정렬 기준 조정 가능:
            // val kidsSorted = kids.sortedBy { it.createdAt } // 혹은 sortedByDescending
            kids.forEach { child ->
                flattened += child.toDTO(children = emptyList())
            }
        }

        return CommonResult(
            state = CRUDStateEnum.READ_SUCCESS,
            message = "",
            data = flattened
        )
    }


    private fun CommentEntity.toDTO(children: List<CommentDTO>): CommentDTO =
        CommentDTO(
            id = this.id!!,
            postId = this.post?.id ?: -1,
            parentId = this.parentId,
            body = this.body,
            isDel = this.isDel,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            children = children
        )

}
