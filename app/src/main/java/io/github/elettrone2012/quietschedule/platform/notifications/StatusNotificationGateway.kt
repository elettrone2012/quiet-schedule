package io.github.elettrone2012.quietschedule.platform.notifications

import java.time.LocalDateTime
import java.time.LocalTime

interface StatusNotificationGateway {

    fun showActive(
        profileName: String,
        endTime: LocalTime
    )

    fun showNextEvent(
        profileName: String,
        dateTime: LocalDateTime
    )

    fun showInactive()

    fun cancel()
}

object NoOpStatusNotificationGateway :
    StatusNotificationGateway {

    override fun showActive(
        profileName: String,
        endTime: LocalTime
    ) = Unit

    override fun showNextEvent(
        profileName: String,
        dateTime: LocalDateTime
    ) = Unit

    override fun showInactive() = Unit

    override fun cancel() = Unit
}