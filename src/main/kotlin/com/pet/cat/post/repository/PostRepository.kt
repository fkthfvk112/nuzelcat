package com.pet.cat.post.repository

import com.pet.cat.post.entity.PostEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface PostRepository: JpaRepository<PostEntity, Long> {

    interface PostDetailRow {
        fun getPostId(): Long
        fun getTitle(): String?
        fun getCatName(): String?
        fun getAuthor(): String?
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

    interface ImageCardRow {
        fun getPostId(): Long
        fun getRepreImgUrl(): String?
        fun getImgCnt(): Long
        fun getCreatedAt(): LocalDateTime?
        fun getTitle(): String?
        fun getCatName(): String?
        fun getTagsConcat(): String?
        fun getLikeCnt(): Long
        fun getViewCnt(): Long
    }

    @Query(
        value = """
    SELECT 
        p.post_id  AS postId,
        (SELECT i.storage_path 
           FROM image i 
          WHERE i.post_id = p.post_id 
          ORDER BY i.image_order ASC, i.image_id ASC 
          LIMIT 1) AS repreImgUrl,
        (SELECT COUNT(*) FROM image i WHERE i.post_id = p.post_id) AS imgCnt,
        p.created_at AS createdAt,
        p.title AS title,
        p.cat_name AS catName,
        COALESCE(GROUP_CONCAT(DISTINCT t_all.tag ORDER BY t_all.tag SEPARATOR '||'), '') AS tagsConcat,
        (SELECT COUNT(*) FROM post_like pl WHERE pl.post_id = p.post_id) AS likeCnt,
        (SELECT COUNT(*) FROM post_view pv WHERE pv.post_id = p.post_id) AS viewCnt
    FROM post p
    LEFT JOIN post_tag t_all ON t_all.post_id = p.post_id
    WHERE p.is_del = 0
      AND (:title   IS NULL OR p.title    LIKE %:title%)
      AND (:catName IS NULL OR p.cat_name LIKE %:catName%)
      AND (
            :applyTag = 0
            OR EXISTS ( 
                SELECT 1
                FROM post_tag tx
                WHERE tx.post_id = p.post_id
                  AND tx.tag IN (:tags)
            )
          )
      AND (:fromYMD IS NULL OR p.create_ymd >= :fromYMD)
      AND (:toYMD   IS NULL OR p.create_ymd <= :toYMD)
      AND (:exceptId IS NULL OR p.post_id <> :exceptId)
    GROUP BY p.post_id
    ORDER BY 
        CASE :sortDir 
            WHEN 'desc'       THEN p.created_at
            WHEN 'score_desc' THEN p.score_popular
            ELSE 0 
        END DESC,
        CASE :sortDir
            WHEN 'asc'        THEN p.created_at 
            WHEN 'score_asc'  THEN p.score_popular
            ELSE 0
        END ASC
      ,p.post_id DESC
    """,
        countQuery = """
    SELECT COUNT(*)
    FROM post p
    WHERE p.is_del = 0
      AND (:title   IS NULL OR p.title    LIKE %:title%)
      AND (:catName IS NULL OR p.cat_name LIKE %:catName%)
      AND (
            :applyTag = 0
            OR EXISTS (
                SELECT 1
                FROM post_tag tx
                WHERE tx.post_id = p.post_id
                  AND tx.tag IN (:tags)
            )
          )
      AND (:fromYMD IS NULL OR p.create_ymd >= :fromYMD)
      AND (:toYMD   IS NULL OR p.create_ymd <= :toYMD)
      AND (:exceptId IS NULL OR p.post_id <> :exceptId)
    """,
        nativeQuery = true
    )
    fun findImageCards(
        @Param("title") title: String?,
        @Param("catName") catName: String?,
        @Param("tags") tags: List<String>?,      // 적용 시만 사용
        @Param("applyTag") applyTag: Int,        // 0 or 1
        @Param("sortDir") sortDir: String,
        @Param("fromYMD") fromYMD: String?,
        @Param("toYMD") toYMD: String?,
        @Param("exceptId") exceptId: String?,
        pageable: Pageable
    ): Page<ImageCardRow>


    @Modifying
    @Transactional
    @Query(
        value = """
        UPDATE post p
        JOIN (
            SELECT 
                p2.post_id AS post_id,
                (
                    (LN(1 + IFNULL(l.like_cnt, 0)) * :wLike)
                  + (LN(1 + IFNULL(v.view_cnt, 0)) * :wView)
                  + (LN(1 + IFNULL(c.comment_cnt, 0)) * :wComment)
                ) * POW(2, - TIMESTAMPDIFF(HOUR, p2.created_at, NOW()) / :halfLifeHours) AS new_score
            FROM post p2
            LEFT JOIN (
                SELECT pl.post_id, COUNT(*) AS like_cnt
                FROM post_like pl
                GROUP BY pl.post_id
            ) l ON l.post_id = p2.post_id
            LEFT JOIN (
                SELECT pv.post_id, COUNT(*) AS view_cnt
                FROM post_view pv
                GROUP BY pv.post_id
            ) v ON v.post_id = p2.post_id
            LEFT JOIN (
                SELECT c.post_id, COUNT(*) AS comment_cnt
                FROM comment c
                WHERE IFNULL(c.is_del, 0) = 0
                GROUP BY c.post_id
            ) c ON c.post_id = p2.post_id
            WHERE p2.is_del = 0
              AND TIMESTAMPDIFF(DAY, p2.created_at, NOW()) <= :maxAgeDays
        ) s ON s.post_id = p.post_id
        SET p.score_popular = ROUND(s.new_score, 4)
        WHERE s.new_score >= :minScore
        """,
        nativeQuery = true
    )
    fun recomputePopularityScore(
        @Param("wLike") wLike: Double = 1.0,
        @Param("wView") wView: Double = 0.3,
        @Param("wComment") wComment: Double = 0.8,
        @Param("halfLifeHours") halfLifeHours: Int = 24 * 14, // 14일 half-life
        @Param("maxAgeDays") maxAgeDays: Int = 150,
        @Param("minScore") minScore: Double = 0.05
    ): Int

    @Query(
        value = """
        SELECT p.post_id
        FROM post p
        WHERE p.is_del = 0
        """,
        nativeQuery = true
    )
    fun getNotDeletedPostIdList(): List<Long>

    @Query(
        value = """
        SELECT COUNT(*)
        FROM post p
        WHERE p.is_del = 0
    """,
        nativeQuery = true
    )
    fun countNotDeletedPosts(): Long
}