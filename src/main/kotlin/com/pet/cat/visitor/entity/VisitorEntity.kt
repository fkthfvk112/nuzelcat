package com.pet.cat.visitor.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "visitor")
class VisitorEntity(
    @Id
    @Column(name = "visitor_id", length = 36)
    var id: String? = null,//uuid

    @Column(name = "remote_ip", length = 64)
    var remoteIp: String? = null,

    @Column(name = "user_agent", length = 255)
    var userAgent: String? = null,

    @Column(name = "accept_language", length = 64)
    var acceptLanguage: String? = null,

    @Column(name = "tz_offset_minutes")
    var tzOffsetMinutes: Int? = null,

    @Column(name = "referrer", length = 255)
    var referrer: String? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "last_seen_at")
    var lastSeenAt: LocalDateTime? = null

){
    @PrePersist
    fun ensureId() {
        if (id == null) {
            id = UUID.randomUUID().toString()
        }
    }
}