package com.pet.cat.post.repository

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.post.entity.PostLikeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PostLikeRepository: JpaRepository<PostLikeEntity, Long> {
    @Query(
        """
        SELECT COUNT(pl) > 0 
        FROM PostLikeEntity pl 
        WHERE pl.post.id = :postId 
          AND pl.visitor.id = :visitorId 
          AND DATE(pl.createdAt) = CURRENT_DATE
        """
    )
    fun existsTodayLike(
        @Param("postId") postId: Long,
        @Param("visitorId") visitorId: String
    ): Boolean

    fun countByPostId(postId: Long): Long
}