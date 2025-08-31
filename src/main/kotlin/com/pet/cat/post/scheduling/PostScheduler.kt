package com.pet.cat.post.scheduling

import com.pet.cat.post.repository.PostRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional

class PostScheduler(
    private val postRepository: PostRepository,
    ) {
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    fun recompute() {
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