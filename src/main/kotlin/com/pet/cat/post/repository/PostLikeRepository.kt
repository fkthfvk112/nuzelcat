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
        value = """
            SELECT EXISTS (
                SELECT 1 
                FROM post_like pl
                WHERE pl.post_id = :postId
                  AND pl.visitor_id = :visitorId
                  AND DATE(pl.created_at) = CURDATE()
            )
        """,
        nativeQuery = true
    )
    fun existsTodayLike(
        @Param("postId") postId: Long,
        @Param("visitorId") visitorId: String
    ): Long

    fun countByPostId(postId: Long): Long
}