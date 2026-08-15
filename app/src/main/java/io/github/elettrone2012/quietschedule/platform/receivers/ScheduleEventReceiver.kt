package io.github.elettrone2012.quietschedule.platform.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.elettrone2012.quietschedule.app.QuietScheduleFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ScheduleEventReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val coordinator =
                    QuietScheduleFactory.createCoordinator(context)

                coordinator.reconcile(
                    now = LocalDateTime.now()
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}