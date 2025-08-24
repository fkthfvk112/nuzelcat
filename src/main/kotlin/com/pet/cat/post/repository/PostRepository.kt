package com.pet.cat.post.repository

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.post.entity.PostEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PostRepository: JpaRepository<PostEntity, Long> {

    interface PostDetailRow {
        fun getPostId(): Long
        fun getTitle(): String?
        fun getCatName(): String
        fun getAuthor(): String
        fun getDescription(): String?
        fun getCreatedAt(): LocalDateTime?

        // GROUP_CONCAT 결과
        fun getImagesConcat(): String?
        fun getTagsConcat(): String?

        fun getLikeCnt(): Long
        fun getViewCnt(): Long
    }

    @Query(
        value = """
        SELECT 
            p.post_id                         AS postId,
            p.title                           AS title,
            p.cat_name                        AS catName,
            p.author_nickname                 AS author,
            p.description                     AS description,
            p.created_at                      AS createdAt,
            COALESCE(
                GROUP_CONCAT(DISTINCT i.storage_path ORDER BY i.image_order, i.image_id SEPARATOR '||'),
                ''
            )                                  AS imagesConcat,
            COALESCE(
                GROUP_CONCAT(DISTINCT t.tag ORDER BY t.tag_id SEPARATOR '||'),
                ''
            )                                  AS tagsConcat,
            (SELECT COUNT(*) FROM post_like  pl WHERE pl.post_id = p.post_id) AS likeCnt,
            (SELECT COUNT(*) FROM post_view  pv WHERE pv.post_id = p.post_id) AS viewCnt
        FROM post p
        LEFT JOIN image     i ON i.post_id = p.post_id
        LEFT JOIN post_tag  t ON t.post_id = p.post_id
        WHERE p.is_del = 0
          AND p.post_id = :postId
        GROUP BY p.post_id
        """,
        nativeQuery = true
    )
    fun findDetailByPostId(@Param("postId") postId: Long): PostDetailRow?
}