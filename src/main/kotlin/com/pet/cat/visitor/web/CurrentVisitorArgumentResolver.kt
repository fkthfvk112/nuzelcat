package com.pet.cat.visitor.web

import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity
import com.pet.cat.visitor.repository.VisitorRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class CurrentVisitorArgumentResolver(
    private val visitorRepo: VisitorRepository
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.parameterType == CurrentVisitorDto::class.java

    override fun resolveArgument(
        parameter: MethodParameter, mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?
    ): Any? {
        // 필터에서 세팅한 값 사용
        val visitor = webRequest.getAttribute(VisitorRequestAttributes.ATTR_KEY, RequestAttributes.SCOPE_REQUEST)
                as? VisitorEntity
        return visitor?.let { CurrentVisitorDto(it.id, it.remoteIp, it.userAgent, it.acceptLanguage, it.tzOffsetMinutes) }
    }
}