package io.github.elettrone2012.quietschedule.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class PersistedProfile(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val dndPolicy: PersistedDndPolicy,
    val schedules: List<PersistedSchedule>
)

@Serializable
data class PersistedSchedule(
    val daysOfWeek: List<String>,
    val startTime: String,
    val endTime: String
)

@Serializable
data class PersistedDndPolicy(
    val allowAlarms: Boolean = false,
    val allowReminders: Boolean = false,
    val allowEvents: Boolean = false,
    val allowMedia: Boolean = false,
    val allowSystem: Boolean = false,
    val allowRepeatCallers: Boolean = false,
    val calls: PersistedSenderCategory = PersistedSenderCategory(),
    val messages: PersistedSenderCategory = PersistedSenderCategory(),
    val conversations: PersistedConversationCategory = PersistedConversationCategory(),
    val suppressedVisualEffects: PersistedSuppressedVisualEffects =
        PersistedSuppressedVisualEffects()
)

@Serializable
data class PersistedSenderCategory(
    val enabled: Boolean = false,
    val sender: String = "ANYONE"
)

@Serializable
data class PersistedConversationCategory(
    val enabled: Boolean = false,
    val sender: String = "ANYONE"
)

@Serializable
data class PersistedSuppressedVisualEffects(
    val fullScreenIntent: Boolean = false,
    val lights: Boolean = false,
    val peek: Boolean = false,
    val statusBar: Boolean = false,
    val badge: Boolean = false,
    val ambient: Boolean = false,
    val notificationList: Boolean = false
)