package com.pet.cat.post.entity

import com.pet.cat.visitor.entity.VisitorEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "comment")
class CommentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: PostEntity? = null,

    @Column(name = "parent_id")
    var parentId: Long? = null,

    @Column(name = "body", nullable = false)
    var body: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    var visitor: VisitorEntity? = null,

    @Column(name = "is_del", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    var isDel: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
)