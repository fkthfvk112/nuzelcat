package com.pet.cat.post.repository

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.post.entity.PostLikeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PostLikeRepository: JpaRepository<PostLikeEntity, Long> {
    fun countByPostIdAndVisitorIdAndCreatedAtBetween(
        postId: Long,
        visitorId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): Long
    fun countByPostId(postId: Long): Long
}