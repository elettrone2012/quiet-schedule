package io.github.elettrone2012.quietschedule.platform.dnd

import android.content.Context
import android.os.Build
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy

class DndController(
    context: Context
) : DndGateway {

    private val delegate: DndGateway =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            DndControllerApi35(
                context.applicationContext
            )
        } else {
            DndControllerApi30(
                context.applicationContext
            )
        }

    override fun hasPolicyAccess(): Boolean {
        return delegate.hasPolicyAccess()
    }

    override fun applyPriorityMode(
        policy: DndPolicy
    ) {
        delegate.applyPriorityMode(policy)
    }

    override fun disableDnd() {
        delegate.disableDnd()
    }
}