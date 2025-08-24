package com.pet.cat.visitor.repository

import com.pet.cat.visitor.entity.VisitorEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface VisitorRepository: JpaRepository<VisitorEntity, String> {
    fun findFirstByRemoteIp(ipHash: String): VisitorEntity?

    @Query("""
            SELECT v
            FROM VisitorEntity v
            WHERE v.remoteIp        = :remoteIp
              AND v.userAgent       = :userAgent
              AND v.acceptLanguage  = :acceptLanguage
              AND (
                    (:tzOffsetMinutes IS NULL AND v.tzOffsetMinutes IS NULL)
                    OR v.tzOffsetMinutes = :tzOffsetMinutes
              )
        """)
    fun findByIpUaAlTm(
        @Param("remoteIp") remoteIp: String,
        @Param("userAgent") userAgent: String,
        @Param("acceptLanguage") acceptLanguage: String,
        @Param("tzOffsetMinutes") tzOffsetMinutes: Int?
    ): VisitorEntity?

}