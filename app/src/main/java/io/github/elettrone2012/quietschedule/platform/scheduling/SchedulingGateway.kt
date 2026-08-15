package io.github.elettrone2012.quietschedule.platform.scheduling

import io.github.elettrone2012.quietschedule.domain.scheduling.ScheduleEvent

interface SchedulingGateway {

    fun scheduleNext(
        event: ScheduleEvent?
    )

    fun cancelAll()
}