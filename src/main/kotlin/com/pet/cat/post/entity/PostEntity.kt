package com.pet.cat.post.entity

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.visitor.entity.VisitorEntity
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity
@Table(name = "post")
class PostEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    var poster: VisitorEntity? = null,

    @Column(name = "title", length = 80)
    var title: String? = null,

    @Column(name = "cat_name", length = 40, nullable = false)
    var catName: String?,

    @Column(name = "pw", length = 256, nullable = true)
    var pw:String?,

    @Column(name = "author_nickname", length = 40, nullable = false)
    var authorNickname: String?,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "location_region", length = 60)
    var locationRegion: String? = null,

    @Column(name = "is_del", columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    var isDel: Boolean = false,

    @Column(name = "score_popular", precision = 12, scale = 4, nullable = false)
    var scorePopular: java.math.BigDecimal = java.math.BigDecimal.ZERO,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @Column(name = "create_ymd")
    var createYMD:String? = null,

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
){
    /** 엔티티가 처음 persist 되기 전에 create_ymd 자동 세팅 */
    @PrePersist
    fun onPrePersist() {
        if (createYMD.isNullOrBlank()) {
            createYMD = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            // BASIC_ISO_DATE = yyyyMMdd
        }
    }

    /** 포스트에 속한 이미지들 (여러 장) */
    @OneToMany(
        mappedBy      = "post",
        fetch         = FetchType.LAZY,
        orphanRemoval = true,
        cascade       = [CascadeType.ALL]
    )
    @OrderBy("imageOrder ASC, id ASC")
    var images: MutableList<ImageEntity> = mutableListOf()

    /** 양방향 편의 메서드 */
    fun addImage(image: ImageEntity) {
        images.add(image)
        image.post = this
    }
    fun removeImage(image: ImageEntity) {
        images.remove(image)
        image.post = null
    }
}