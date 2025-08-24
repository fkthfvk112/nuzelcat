package com.pet.cat.post.repository

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.post.entity.PostLikeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostLikeRepository: JpaRepository<PostLikeEntity, Long> {
}