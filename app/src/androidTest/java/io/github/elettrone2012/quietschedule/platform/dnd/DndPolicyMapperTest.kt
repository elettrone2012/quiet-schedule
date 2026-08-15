package io.github.elettrone2012.quietschedule.platform.dnd

import android.app.NotificationManager
import io.github.elettrone2012.quietschedule.domain.model.ConversationCategory
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.SenderCategory
import io.github.elettrone2012.quietschedule.domain.model.SenderScope
import io.github.elettrone2012.quietschedule.domain.model.SuppressedVisualEffects
import org.junit.Assert
import org.junit.Test

class DndPolicyMapperTest {

    @Test
    fun disabledCallsDoNotEnableCallsCategory() {
        val policy = DndPolicy(
            calls = SenderCategory(
                enabled = false,
                sender = SenderScope.STARRED
            )
        )

        val androidPolicy = policy.toAndroidPolicy()

        Assert.assertFalse(
            androidPolicy.priorityCategories and
                    NotificationManager.Policy.PRIORITY_CATEGORY_CALLS != 0
        )
    }

    @Test
    fun enabledCallsMapSenderScope() {
        val policy = DndPolicy(
            calls = SenderCategory(
                enabled = true,
                sender = SenderScope.CONTACTS
            )
        )

        val androidPolicy = policy.toAndroidPolicy()

        Assert.assertTrue(
            androidPolicy.priorityCategories and
                    NotificationManager.Policy.PRIORITY_CATEGORY_CALLS != 0
        )

        Assert.assertEquals(
            NotificationManager.Policy.PRIORITY_SENDERS_CONTACTS,
            androidPolicy.priorityCallSenders
        )
    }

    @Test
    fun enabledMessagesMapSenderScope() {
        val policy = DndPolicy(
            messages = SenderCategory(
                enabled = true,
                sender = SenderScope.STARRED
            )
        )

        val androidPolicy = policy.toAndroidPolicy()

        Assert.assertTrue(
            androidPolicy.priorityCategories and
                    NotificationManager.Policy.PRIORITY_CATEGORY_MESSAGES != 0
        )

        Assert.assertEquals(
            NotificationManager.Policy.PRIORITY_SENDERS_STARRED,
            androidPolicy.priorityMessageSenders
        )
    }

    @Test
    fun disabledConversationsMapToNone() {
        val policy = DndPolicy(
            conversations = ConversationCategory(
                enabled = false,
                sender = ConversationScope.IMPORTANT
            )
        )

        val androidPolicy = policy.toAndroidPolicy()

        Assert.assertFalse(
            androidPolicy.priorityCategories and
                    NotificationManager.Policy.PRIORITY_CATEGORY_CONVERSATIONS != 0
        )

        Assert.assertEquals(
            NotificationManager.Policy.CONVERSATION_SENDERS_NONE,
            androidPolicy.priorityConversationSenders
        )
    }

    @Test
    fun enabledConversationsMapImportantScope() {
        val policy = DndPolicy(
            conversations = ConversationCategory(
                enabled = true,
                sender = ConversationScope.IMPORTANT
            )
        )

        val androidPolicy = policy.toAndroidPolicy()

        Assert.assertTrue(
            androidPolicy.priorityCategories and
                    NotificationManager.Policy.PRIORITY_CATEGORY_CONVERSATIONS != 0
        )

        Assert.assertEquals(
            NotificationManager.Policy.CONVERSATION_SENDERS_IMPORTANT,
            androidPolicy.priorityConversationSenders
        )
    }

    @Test
    fun visualEffectsMapIndependently() {
        val policy = DndPolicy(
            suppressedVisualEffects = SuppressedVisualEffects(
                fullScreenIntent = true,
                lights = true,
                peek = true
            )
        )

        val androidPolicy = policy.toAndroidPolicy()

        Assert.assertTrue(
            androidPolicy.suppressedVisualEffects and
                    NotificationManager.Policy.SUPPRESSED_EFFECT_FULL_SCREEN_INTENT != 0
        )

        Assert.assertTrue(
            androidPolicy.suppressedVisualEffects and
                    NotificationManager.Policy.SUPPRESSED_EFFECT_LIGHTS != 0
        )

        Assert.assertTrue(
            androidPolicy.suppressedVisualEffects and
                    NotificationManager.Policy.SUPPRESSED_EFFECT_PEEK != 0
        )
    }
}