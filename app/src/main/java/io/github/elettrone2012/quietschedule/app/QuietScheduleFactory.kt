package io.github.elettrone2012.quietschedule.app

import android.content.Context
import io.github.elettrone2012.quietschedule.data.datastore.QuietScheduleRepository
import io.github.elettrone2012.quietschedule.platform.dnd.DndController
import io.github.elettrone2012.quietschedule.platform.notifications.StatusNotificationManager
import io.github.elettrone2012.quietschedule.platform.scheduling.AlarmSchedulingGateway

object QuietScheduleFactory {

    fun createCoordinator(
        context: Context
    ): QuietScheduleCoordinator {
        val appContext =
            context.applicationContext

        return QuietScheduleCoordinator(
            repository =
                QuietScheduleRepository(
                    appContext
                ),
            dndGateway =
                DndController(
                    appContext
                ),
            schedulingGateway =
                AlarmSchedulingGateway(
                    appContext
                ),
            statusNotificationGateway =
                StatusNotificationManager(
                    appContext
                )
        )
    }
}