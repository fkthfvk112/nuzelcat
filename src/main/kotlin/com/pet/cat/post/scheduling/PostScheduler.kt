package com.pet.cat.post.scheduling

import com.pet.cat.post.repository.PostRepository
import lombok.extern.java.Log
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PostScheduler(
    private val postRepository: PostRepository,
    ) {
    private val log = LoggerFactory.getLogger(PostScheduler::class.java)

    @Scheduled(cron = "0 56 21 * * *", zone = "Asia/Seoul")
    @Transactional
    fun recompute() {
        log.info("[PostScheduler]-recompute start")
        val updatedRows = postRepository.recomputePopularityScore(
            wLike = 1.0,
            wView = 0.3,
            wComment = 0.8,
            halfLifeHours = 24 * 14,
            maxAgeDays = 150,
            minScore = 0.05
        )
    }
}