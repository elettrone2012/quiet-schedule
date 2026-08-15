package io.github.elettrone2012.quietschedule.app

import io.github.elettrone2012.quietschedule.data.datastore.QuietScheduleRepositoryContract
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.profile.disableAllProfiles
import io.github.elettrone2012.quietschedule.domain.scheduling.findActiveProfile
import io.github.elettrone2012.quietschedule.domain.scheduling.findNextScheduleEvent
import io.github.elettrone2012.quietschedule.domain.scheduling.isActiveAt
import io.github.elettrone2012.quietschedule.platform.dnd.DndGateway
import io.github.elettrone2012.quietschedule.platform.notifications.NoOpStatusNotificationGateway
import io.github.elettrone2012.quietschedule.platform.notifications.StatusNotificationGateway
import io.github.elettrone2012.quietschedule.platform.scheduling.SchedulingGateway
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class QuietScheduleCoordinator(
    private val repository: QuietScheduleRepositoryContract,
    private val dndGateway: DndGateway,
    private val schedulingGateway: SchedulingGateway,
    private val statusNotificationGateway:
    StatusNotificationGateway =
        NoOpStatusNotificationGateway
) {

    suspend fun reconcile(
        now: LocalDateTime
    ) {
        var profiles =
            repository.profiles.first()

        val appSettings =
            repository.appSettings.first()

        if (!dndGateway.hasPolicyAccess()) {
            profiles =
                disableAllProfiles(
                    profiles
                )

            repository.saveProfiles(
                profiles
            )

            schedulingGateway.cancelAll()

            updateStatusNotification(
                profiles = profiles,
                showStatusNotification =
                    appSettings.showStatusNotification,
                now = now
            )

            return
        }

        val activeProfile =
            findActiveProfile(
                profiles = profiles,
                dayOfWeek = now.dayOfWeek,
                time = now.toLocalTime()
            )

        if (activeProfile == null) {
            dndGateway.disableDnd()
        } else {
            dndGateway.applyPriorityMode(
                activeProfile.dndPolicy
            )
        }

        val nextEvent =
            findNextScheduleEvent(
                profiles = profiles,
                now = now
            )

        schedulingGateway.scheduleNext(
            nextEvent
        )

        updateStatusNotification(
            profiles = profiles,
            showStatusNotification =
                appSettings.showStatusNotification,
            now = now
        )
    }

    private fun updateStatusNotification(
        profiles: List<Profile>,
        showStatusNotification: Boolean,
        now: LocalDateTime
    ) {
        if (!showStatusNotification) {
            statusNotificationGateway.cancel()
            return
        }

        val enabledProfiles =
            profiles.filter {
                it.enabled
            }

        if (enabledProfiles.isEmpty()) {
            statusNotificationGateway
                .showInactive()

            return
        }

        val activeProfile =
            findActiveProfile(
                profiles = profiles,
                dayOfWeek = now.dayOfWeek,
                time = now.toLocalTime()
            )

        if (activeProfile != null) {
            val activeSchedule =
                activeProfile.schedules
                    .firstOrNull {
                        it.isActiveAt(
                            dayOfWeek =
                                now.dayOfWeek,
                            time =
                                now.toLocalTime()
                        )
                    }

            if (activeSchedule != null) {
                statusNotificationGateway
                    .showActive(
                        profileName =
                            activeProfile.name,
                        endTime =
                            activeSchedule.endTime
                    )

                return
            }
        }

        val nextEvent =
            findNextScheduleEvent(
                profiles = profiles,
                now = now
            )

        if (nextEvent != null) {
            statusNotificationGateway
                .showNextEvent(
                    profileName =
                        nextEvent.profile.name,
                    dateTime =
                        nextEvent.dateTime
                )
        } else {
            statusNotificationGateway
                .showInactive()
        }
    }
}