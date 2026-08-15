package io.github.elettrone2012.quietschedule.platform.dnd

import android.app.NotificationManager
import android.content.Context
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy

class DndControllerApi30(
    context: Context
) : DndGateway {

    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    override fun hasPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    override fun applyPriorityMode(
        policy: DndPolicy
    ) {
        notificationManager.notificationPolicy =
            policy.toAndroidPolicy()

        notificationManager.setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_PRIORITY
        )
    }

    override fun disableDnd() {
        notificationManager.setInterruptionFilter(
            NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }
}