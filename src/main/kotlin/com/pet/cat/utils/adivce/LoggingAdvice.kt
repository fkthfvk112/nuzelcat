package com.pet.cat.utils.adivce

import com.pet.cat.utils.dto.LogInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class LoggingAdvice {
    companion object {
        fun <R> infolog(funcName: String, function: () -> R): R {
            val log: Logger = LoggerFactory.getLogger(this::class.java)

            val startAt = LocalDateTime.now()
            val res = function.invoke()
            val endAt = LocalDateTime.now()

            val logInfo: LogInfo = LogInfo(
                level = "info",
                startTime = startAt,
                endTime = endAt,
                funcName = funcName
            )

            log.info(logInfo.toString())

            return res
        }
    }
}