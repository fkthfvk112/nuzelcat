package com.pet.cat.image.entity

import com.pet.cat.post.entity.PostEntity
import com.pet.cat.visitor.entity.VisitorEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name="image")
class ImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    var id: Long? = null,

    @Column(name = "image_sort", length = 30, nullable = false)
    var imageSort: String,

    @Column(name = "storage_path", length = 1024, nullable = false)
    var storagePath: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: PostEntity? = null,

    @Column(name = "image_order")
    var imageOrder: Int? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null
)