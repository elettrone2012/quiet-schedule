package io.github.elettrone2012.quietschedule.domain.model

import java.util.UUID

data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = false,
    val dndPolicy: DndPolicy = DndPolicy(),
    val schedules: List<Schedule>
)