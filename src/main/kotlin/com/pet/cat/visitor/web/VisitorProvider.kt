package com.pet.cat.visitor.web

import com.pet.cat.visitor.entity.VisitorEntity
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class VisitorProvider {
    fun get(request: HttpServletRequest): VisitorEntity? =
        request.getAttribute(VisitorRequestAttributes.ATTR_KEY) as? VisitorEntity
}