package com.pet.cat.visitor.web

import com.pet.cat.visitor.entity.SiteAccessEntity
import com.pet.cat.visitor.entity.VisitorEntity
import com.pet.cat.visitor.repository.VisitorRepository
import com.pet.cat.visitor.repository.SiteAccessRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Component
class VisitorIdentificationFilter(
    private val visitorRepository: VisitorRepository,
    private val siteAccessRepository: SiteAccessRepository,
    @Value("\${app.visitor.cookie-name:visitor_id}") private val visitorCookieName: String = "visitor_id"
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        try {
            val visitor = identifyOrCreateVisitor(request, response)
            request.setAttribute(VisitorRequestAttributes.ATTR_KEY, visitor)
        } catch (_: Exception) {
            // 식별 실패해도 서비스는 계속 (익명 null 가능)
        }
        chain.doFilter(request, response)
    }

    /*
    동일인인지 확인
    1) 동일인 쿠키를 지닌 경우 or
    2) ip, ua, lang, tz가 일치하는 경우
     */
    private fun identifyOrCreateVisitor(req: HttpServletRequest, res: HttpServletResponse): VisitorEntity {
        return try {
            val cookieVisitorId = req.cookies?.firstOrNull { it.name == visitorCookieName }?.value

            val ua = req.getHeader("User-Agent")
            val lang = req.getHeader("Accept-Language")
            val tz = req.getHeader("X-TZ-Offset-Min")?.toIntOrNull()
            val ref = req.getHeader("Referer")

            val remoteIp = extractClientIp(req)

            if (isIgnoredBot(ua)) {
                return VisitorEntity(
                    remoteIp = remoteIp,
                    userAgent = "bot_user",
                    acceptLanguage = lang,
                    tzOffsetMinutes = tz,
                    referrer = ref,
                    lastSeenAt = LocalDateTime.now()
                )
            }

            // 1) 쿠키로 찾기
            var visitorEntity = cookieVisitorId?.let { visitorRepository.findById(it).orElse(null) }

            // 2) 쿠키가 없는 경우 ip, ua, lang, tz로 찾기
            if (visitorEntity == null) {
                visitorEntity = visitorRepository.findByIpUaAlTm(remoteIp, ua, lang, tz)
            }

            // 방문 이력이 있는 경우 업데이트 후 반환
            if (visitorEntity != null) {
                val lastSeen = visitorEntity.lastSeenAt
                updateVisitor(visitorEntity, remoteIp, ua, lang, tz, ref)
                recordVisit(visitorEntity, lastSeen, true)
                return visitorEntity
            }

            // 신규 생성
            val createdVisitorEntity = visitorRepository.save(
                VisitorEntity(
                    remoteIp = remoteIp,
                    userAgent = ua,
                    acceptLanguage = lang,
                    tzOffsetMinutes = tz,
                    referrer = ref,
                    lastSeenAt = LocalDateTime.now()
                )
            )

            val lastSeen = createdVisitorEntity.lastSeenAt
            recordVisit(createdVisitorEntity, lastSeen, false)
            setCookie(res, createdVisitorEntity.id!!)

            createdVisitorEntity
        } catch (e: Exception) {
            // 예외 로그 남기기
            logger.error("Visitor identification failed", e)

            // 최소한의 정보라도 담아서 반환
            val fallbackVisitor = VisitorEntity(
                remoteIp = req.remoteAddr,
                userAgent = req.getHeader("User-Agent"),
                acceptLanguage = req.getHeader("Accept-Language"),
                tzOffsetMinutes = req.getHeader("X-TZ-Offset-Min")?.toIntOrNull(),
                referrer = req.getHeader("Referer"),
                lastSeenAt = LocalDateTime.now()
            )

            // DB 저장은 안 하고 메모리 객체만 반환
            fallbackVisitor
        }
    }

    
    // 방문 기록 남기기, 같은 정보에 대해서는 하루에 한 번만 남기
    private fun recordVisit(visitorEntity: VisitorEntity, lastSeen: LocalDateTime?, usingOneDayLimit:Boolean) {
        if(usingOneDayLimit && !isOneDayPassed(lastSeen)) return

        val now = LocalDateTime.now()
        val siteAccess = SiteAccessEntity(
            ipAddress = visitorEntity.remoteIp,
            userAgent = visitorEntity.userAgent,
            referrer = visitorEntity.referrer,
            accessedAt = now,
            createYMD = now.format(DateTimeFormatter.BASIC_ISO_DATE) // YYYYMMDD
        )

        siteAccessRepository.save(siteAccess) // JPA Repository 사용
    }

    private fun isOneDayPassed(lastSeen: LocalDateTime?): Boolean {
        if (lastSeen == null) return true
        val now = LocalDateTime.now()
        return lastSeen.toLocalDate().isBefore(now.toLocalDate())
    }

    private fun updateVisitor(
        visitorEntity: VisitorEntity, ip: String, ua: String?, lang: String?, tz: Int?, ref: String?
    ) {
        val now = LocalDateTime.now()

        // 최신 정보로 갱신(값이 있으면 덮어쓰기)
        visitorEntity.remoteIp = ip
        visitorEntity.userAgent = ua
        visitorEntity.acceptLanguage = lang
        visitorEntity.tzOffsetMinutes = tz
        visitorEntity.referrer = ref
        visitorEntity.lastSeenAt = now

        visitorRepository.save(visitorEntity)
    }

    private fun setCookie(res: HttpServletResponse, visitorUUId: String) {
        val cookie = Cookie(visitorCookieName, visitorUUId)
        cookie.isHttpOnly = true
        cookie.secure = true
        cookie.path = "/"
        cookie.maxAge = 60 * 60 * 24 * 365 // 1년

        res.addCookie(cookie)
    }

    private fun extractClientIp(req: HttpServletRequest): String {
        val h = listOf(
            "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP",
            "X-Original-Forwarded-For"
        ).firstNotNullOfOrNull { header ->
            req.getHeader(header)?.split(",")?.firstOrNull()?.trim()
        }
        return h ?: req.remoteAddr
    }

    private fun isIgnoredBot(ua: String?): Boolean {
        if (ua == null) return false
        val lower = ua.lowercase()
        return listOf(
            "vercel-favicon",
            "vercel-screenshot",
            "uptime",
            "monitor",
            "bot",
            "crawler",
            "spider"
        ).any { lower.contains(it) }
    }
}