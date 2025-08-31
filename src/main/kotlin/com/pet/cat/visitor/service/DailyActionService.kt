package com.pet.cat.visitor.service

import com.pet.cat.visitor.entity.DailyActionLogEntity
import com.pet.cat.visitor.entity.VisitorEntity
import com.pet.cat.visitor.enums.ActionEnum
import com.pet.cat.visitor.repository.DailyActionLogRepository
import com.pet.cat.visitor.service.Interface.IDailyActionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class DailyActionService(
    val dailyActionLogRepository: DailyActionLogRepository
):IDailyActionService {

    @Transactional
    override fun logDailyAction(visitor: VisitorEntity?, actionType:ActionEnum){
        dailyActionLogRepository.save(DailyActionLogEntity(
            visitor = visitor,
            actionType = actionType.name
        ))

    }
    override fun getTodayActionCnt(actionType: ActionEnum): Long {
        val todayYmd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) // yyyyMMdd
        return dailyActionLogRepository.countByActionTypeAndCreateYmd(
            actionType.name,
            todayYmd
        )
    }
}