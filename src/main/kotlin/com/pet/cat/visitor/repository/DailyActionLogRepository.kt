package com.pet.cat.visitor.repository

import com.pet.cat.visitor.entity.DailyActionLogEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DailyActionLogRepository: JpaRepository<DailyActionLogEntity, Long> {
    fun countByActionTypeAndCreateYmd(actionType: String, createYmd: String): Long
}