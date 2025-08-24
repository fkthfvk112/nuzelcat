package com.pet.cat.visitor.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "site_access")
class SiteAccessEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_access_id")
    var id: Long? = null,
    var ipAddress:String? = null,
    var userAgent:String? = null,
    var referrer:String? = null,
    var accessedAt:LocalDateTime? = null,

    @Column(name = "create_ymd")
    var createYMD:String? = null
)