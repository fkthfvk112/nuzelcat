package com.pet.cat.post.entity

import jakarta.persistence.*

@Entity
@Table(name = "post_tag")
class PostTagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    var post: PostEntity? = null,

    @Column(name = "tag", length = 50, nullable = false)
    var tag: String
)