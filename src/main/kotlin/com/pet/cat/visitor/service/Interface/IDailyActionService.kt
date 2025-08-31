package com.pet.cat.visitor.service.Interface

import com.pet.cat.visitor.entity.VisitorEntity
import com.pet.cat.visitor.enums.ActionEnum

interface IDailyActionService {
    fun logDailyAction(visitor: VisitorEntity?, actionType:ActionEnum)
    fun getTodayActionCnt(actionType: ActionEnum): Long
}