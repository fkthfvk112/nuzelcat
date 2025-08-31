package com.pet.cat.visitor.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(
    name = "daily_action_log",
    indexes = [
        Index(name = "idx_action_lookup", columnList = "action_type, create_ymd, visitor_id")
    ]
)
class DailyActionLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id", nullable = true)
    var visitor: VisitorEntity? = null,   // nullable → 익명 사용자 가능

    @Column(name = "action_type", length = 50, nullable = false)
    var actionType: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @Column(name = "create_ymd", length = 8, nullable = false)
    var createYmd: String? = null
){

    /** 엔티티가 처음 persist 되기 전에 create_ymd 자동 세팅 */
    @PrePersist
    fun onPrePersist() {
        if (createYmd.isNullOrBlank()) {
            createYmd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            // BASIC_ISO_DATE = yyyyMMdd
        }
    }
}