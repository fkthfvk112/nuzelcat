package com.pet.cat.visitor.repository

import com.pet.cat.visitor.entity.SiteAccessEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SiteAccessRepository: JpaRepository<SiteAccessEntity, Long>