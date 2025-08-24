package com.pet.cat.image.repository

import com.pet.cat.image.entity.ImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository:JpaRepository<ImageEntity, Long> {
}