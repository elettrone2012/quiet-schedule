package io.github.elettrone2012.quietschedule.platform.dnd

import android.service.notification.ZenPolicy
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.SenderScope

fun DndPolicy.toZenPolicy(): ZenPolicy {
    val builder = ZenPolicy.Builder()
        .allowAlarms(allowAlarms)
        .allowReminders(allowReminders)
        .allowEvents(allowEvents)
        .allowMedia(allowMedia)
        .allowSystem(allowSystem)
        .allowRepeatCallers(allowRepeatCallers)
        .allowCalls(
            if (calls.enabled) {
                calls.sender.toZenPeopleType()
            } else {
                ZenPolicy.PEOPLE_TYPE_NONE
            }
        )
        .allowMessages(
            if (messages.enabled) {
                messages.sender.toZenPeopleType()
            } else {
                ZenPolicy.PEOPLE_TYPE_NONE
            }
        )
        .allowConversations(
            if (conversations.enabled) {
                conversations.sender.toZenConversationType()
            } else {
                ZenPolicy.CONVERSATION_SENDERS_NONE
            }
        )

    val effects = suppressedVisualEffects

    builder.showFullScreenIntent(!effects.fullScreenIntent)
    builder.showLights(!effects.lights)
    builder.showPeeking(!effects.peek)
    builder.showStatusBarIcons(!effects.statusBar)
    builder.showBadges(!effects.badge)
    builder.showInAmbientDisplay(!effects.ambient)
    builder.showInNotificationList(!effects.notificationList)

    return builder.build()
}

private fun SenderScope.toZenPeopleType(): Int {
    return when (this) {
        SenderScope.ANYONE ->
            ZenPolicy.PEOPLE_TYPE_ANYONE

        SenderScope.CONTACTS ->
            ZenPolicy.PEOPLE_TYPE_CONTACTS

        SenderScope.STARRED ->
            ZenPolicy.PEOPLE_TYPE_STARRED
    }
}

private fun ConversationScope.toZenConversationType(): Int {
    return when (this) {
        ConversationScope.ANYONE ->
            ZenPolicy.CONVERSATION_SENDERS_ANYONE

        ConversationScope.IMPORTANT ->
            ZenPolicy.CONVERSATION_SENDERS_IMPORTANT
    }
}