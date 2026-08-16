package io.github.elettrone2012.quietschedule.data.datastore

import io.github.elettrone2012.quietschedule.domain.model.ConversationCategory
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import io.github.elettrone2012.quietschedule.domain.model.SenderCategory
import io.github.elettrone2012.quietschedule.domain.model.SenderScope
import io.github.elettrone2012.quietschedule.domain.model.SuppressedVisualEffects
import java.time.DayOfWeek


fun Profile.toPersisted(): PersistedProfile {
    return PersistedProfile(
        id = id,
        name = name,
        enabled = enabled,
        dndPolicy = dndPolicy.toPersisted(),
        schedules = schedules.map { it.toPersisted() }
    )
}

fun PersistedProfile.toDomain(): Profile {
    return Profile(
        id = id,
        name = name,
        enabled = enabled,
        dndPolicy = dndPolicy.toDomain(),
        schedules = schedules.map { it.toDomain() }
    )
}

private fun Schedule.toPersisted(): PersistedSchedule {
    return PersistedSchedule(
        daysOfWeek =
            daysOfWeek.map { it.name },
        startMinute =
            startMinute,
        endMinute =
            endMinute
    )
}

private fun PersistedSchedule.toDomain(): Schedule {
    val domainDays =
        daysOfWeek
            .map { DayOfWeek.valueOf(it) }
            .toSet()

    if (
        startMinute != null &&
        endMinute != null
    ) {
        return Schedule(
            daysOfWeek = domainDays,
            startMinute = startMinute,
            endMinute = endMinute
        )
    }

    val legacyStart =
        requireNotNull(startTime) {
            "Missing schedule start time"
        }

    val legacyEnd =
        requireNotNull(endTime) {
            "Missing schedule end time"
        }

    val parsedStart =
        java.time.LocalTime.parse(
            legacyStart
        )

    val parsedEnd =
        java.time.LocalTime.parse(
            legacyEnd
        )

    return Schedule(
        daysOfWeek = domainDays,
        startMinute =
            Schedule.minuteOfDay(
                parsedStart
            ),
        endMinute =
            Schedule.minuteOfDay(
                parsedEnd
            )
    )
}

private fun DndPolicy.toPersisted(): PersistedDndPolicy {
    return PersistedDndPolicy(
        allowAlarms = allowAlarms,
        allowReminders = allowReminders,
        allowEvents = allowEvents,
        allowMedia = allowMedia,
        allowSystem = allowSystem,
        allowRepeatCallers = allowRepeatCallers,
        calls = calls.toPersisted(),
        messages = messages.toPersisted(),
        conversations = conversations.toPersisted(),
        suppressedVisualEffects = suppressedVisualEffects.toPersisted()
    )
}

private fun PersistedDndPolicy.toDomain(): DndPolicy {
    return DndPolicy(
        allowAlarms = allowAlarms,
        allowReminders = allowReminders,
        allowEvents = allowEvents,
        allowMedia = allowMedia,
        allowSystem = allowSystem,
        allowRepeatCallers = allowRepeatCallers,
        calls = calls.toDomain(),
        messages = messages.toDomain(),
        conversations = conversations.toDomain(),
        suppressedVisualEffects = suppressedVisualEffects.toDomain()
    )
}

private fun SenderCategory.toPersisted(): PersistedSenderCategory {
    return PersistedSenderCategory(
        enabled = enabled,
        sender = sender.name
    )
}

private fun PersistedSenderCategory.toDomain(): SenderCategory {
    return SenderCategory(
        enabled = enabled,
        sender = SenderScope.valueOf(sender)
    )
}

private fun ConversationCategory.toPersisted(): PersistedConversationCategory {
    return PersistedConversationCategory(
        enabled = enabled,
        sender = sender.name
    )
}

private fun PersistedConversationCategory.toDomain(): ConversationCategory {
    return ConversationCategory(
        enabled = enabled,
        sender = ConversationScope.valueOf(sender)
    )
}

private fun SuppressedVisualEffects.toPersisted(): PersistedSuppressedVisualEffects {
    return PersistedSuppressedVisualEffects(
        fullScreenIntent = fullScreenIntent,
        lights = lights,
        peek = peek,
        statusBar = statusBar,
        badge = badge,
        ambient = ambient,
        notificationList = notificationList
    )
}

private fun PersistedSuppressedVisualEffects.toDomain(): SuppressedVisualEffects {
    return SuppressedVisualEffects(
        fullScreenIntent = fullScreenIntent,
        lights = lights,
        peek = peek,
        statusBar = statusBar,
        badge = badge,
        ambient = ambient,
        notificationList = notificationList
    )
}