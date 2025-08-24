package com.pet.cat.post.repository

import com.pet.cat.post.entity.PostTagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostTagRepository: JpaRepository<PostTagEntity, Long> {
}