package io.github.elettrone2012.quietschedule.domain.model

data class DndPolicy(
    val allowAlarms: Boolean = false,
    val allowReminders: Boolean = false,
    val allowEvents: Boolean = false,
    val allowMedia: Boolean = false,
    val allowSystem: Boolean = false,
    val allowRepeatCallers: Boolean = false,
    val calls: SenderCategory = SenderCategory(),
    val messages: SenderCategory = SenderCategory(),
    val conversations: ConversationCategory = ConversationCategory(),
    val suppressedVisualEffects: SuppressedVisualEffects = SuppressedVisualEffects()
)

data class SenderCategory(
    val enabled: Boolean = false,
    val sender: SenderScope = SenderScope.ANYONE
)

enum class SenderScope {
    ANYONE,
    CONTACTS,
    STARRED
}

data class ConversationCategory(
    val enabled: Boolean = false,
    val sender: ConversationScope = ConversationScope.ANYONE
)

enum class ConversationScope {
    ANYONE,
    IMPORTANT
}

data class SuppressedVisualEffects(
    val fullScreenIntent: Boolean = false,
    val lights: Boolean = false,
    val peek: Boolean = false,
    val statusBar: Boolean = false,
    val badge: Boolean = false,
    val ambient: Boolean = false,
    val notificationList: Boolean = false
)