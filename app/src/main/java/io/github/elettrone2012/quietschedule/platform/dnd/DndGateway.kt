package io.github.elettrone2012.quietschedule.platform.dnd

import io.github.elettrone2012.quietschedule.domain.model.DndPolicy

interface DndGateway {

    fun hasPolicyAccess(): Boolean

    fun applyPriorityMode(
        policy: DndPolicy
    )

    fun disableDnd()
}