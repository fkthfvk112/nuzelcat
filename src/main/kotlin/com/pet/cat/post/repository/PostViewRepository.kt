package com.pet.cat.post.repository

import com.pet.cat.post.entity.PostTagEntity
import com.pet.cat.post.entity.PostViewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostViewRepository: JpaRepository<PostViewEntity, Long> {
}