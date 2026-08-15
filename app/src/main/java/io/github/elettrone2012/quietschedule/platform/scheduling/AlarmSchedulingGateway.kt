package io.github.elettrone2012.quietschedule.platform.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.github.elettrone2012.quietschedule.domain.scheduling.ScheduleEvent
import io.github.elettrone2012.quietschedule.platform.receivers.ScheduleEventReceiver
import java.time.ZoneId

class AlarmSchedulingGateway(
    private val context: Context
) : SchedulingGateway {

    private val alarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun scheduleNext(
        event: ScheduleEvent?
    ) {
        cancelAll()

        if (event == null) {
            return
        }

        val triggerAtMillis = event.dateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            createPendingIntent()
        )
    }

    override fun cancelAll() {
        alarmManager.cancel(
            createPendingIntent()
        )
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(
            context,
            ScheduleEventReceiver::class.java
        )

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REQUEST_CODE = 1001
    }
}