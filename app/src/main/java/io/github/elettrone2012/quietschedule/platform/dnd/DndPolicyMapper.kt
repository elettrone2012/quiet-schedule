package io.github.elettrone2012.quietschedule.platform.dnd

import android.app.NotificationManager
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.SenderScope

fun DndPolicy.toAndroidPolicy(): NotificationManager.Policy {
    var priorityCategories = 0

    if (allowAlarms) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS
    }

    if (allowReminders) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_REMINDERS
    }

    if (allowEvents) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_EVENTS
    }

    if (allowMedia) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_MEDIA
    }

    if (allowSystem) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_SYSTEM
    }

    if (allowRepeatCallers) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_REPEAT_CALLERS
    }

    if (calls.enabled) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_CALLS
    }

    if (messages.enabled) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_MESSAGES
    }

    if (conversations.enabled) {
        priorityCategories =
            priorityCategories or NotificationManager.Policy.PRIORITY_CATEGORY_CONVERSATIONS
    }

    var suppressedVisualEffects = 0



    val effects = this.suppressedVisualEffects

    if (effects.fullScreenIntent) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_FULL_SCREEN_INTENT
    }

    if (effects.lights) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_LIGHTS
    }

    if (effects.peek) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_PEEK
    }

    if (effects.statusBar) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_STATUS_BAR
    }

    if (effects.badge) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_BADGE
    }

    if (effects.ambient) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_AMBIENT
    }

    if (effects.notificationList) {
        suppressedVisualEffects =
            suppressedVisualEffects or NotificationManager.Policy.SUPPRESSED_EFFECT_NOTIFICATION_LIST
    }

    val callSenders = when (calls.sender) {
        SenderScope.ANYONE ->
            NotificationManager.Policy.PRIORITY_SENDERS_ANY

        SenderScope.CONTACTS ->
            NotificationManager.Policy.PRIORITY_SENDERS_CONTACTS

        SenderScope.STARRED ->
            NotificationManager.Policy.PRIORITY_SENDERS_STARRED
    }

    val messageSenders = when (messages.sender) {
        SenderScope.ANYONE ->
            NotificationManager.Policy.PRIORITY_SENDERS_ANY

        SenderScope.CONTACTS ->
            NotificationManager.Policy.PRIORITY_SENDERS_CONTACTS

        SenderScope.STARRED ->
            NotificationManager.Policy.PRIORITY_SENDERS_STARRED
    }

    val conversationSenders = when {
        !conversations.enabled ->
            NotificationManager.Policy.CONVERSATION_SENDERS_NONE

        conversations.sender == ConversationScope.ANYONE ->
            NotificationManager.Policy.CONVERSATION_SENDERS_ANYONE

        else ->
            NotificationManager.Policy.CONVERSATION_SENDERS_IMPORTANT
    }

    return NotificationManager.Policy(
        priorityCategories,
        callSenders,
        messageSenders,
        suppressedVisualEffects,
        conversationSenders
    )
}