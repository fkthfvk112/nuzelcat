package com.pet.cat.post.repository

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.post.entity.CommentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository: JpaRepository<CommentEntity, Long> {
    fun findByIdAndIsDelFalse(id: Long): CommentEntity?

    fun findByPost_IdAndParentIdIsNullAndIsDelFalseOrderByCreatedAtDesc(postId: Long): List<CommentEntity>

    fun findByPost_IdAndParentIdAndIsDelFalseOrderByCreatedAtAsc(postId: Long, parentId: Long): List<CommentEntity>

    @Query(
        """
        select c from CommentEntity c
        where c.post.id = :postId
          and c.parentId in :parentIds
          and c.isDel = false
        order by c.createdAt asc
        """
    )
    fun findChildrenByParentIds(
        @Param("postId") postId: Long,
        @Param("parentIds") parentIds: List<Long>
    ): List<CommentEntity>
}
