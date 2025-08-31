package com.pet.cat.post.repository

import com.pet.cat.post.entity.PostTagEntity
import com.pet.cat.post.entity.PostViewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PostViewRepository: JpaRepository<PostViewEntity, Long> {
    @Query(
        """
    SELECT COUNT(pv) 
    FROM PostViewEntity pv
    WHERE pv.post.id = :postId
      AND pv.visitor.id = :visitorId
      AND pv.createdAt >= CURRENT_DATE
      AND pv.createdAt < CURRENT_DATE + 1
    """
    )
    fun countTodayView(
        @Param("postId") postId: Long,
        @Param("visitorId") visitorId: String
    ): Long

    fun countByPostId(postId: Long): Long
}