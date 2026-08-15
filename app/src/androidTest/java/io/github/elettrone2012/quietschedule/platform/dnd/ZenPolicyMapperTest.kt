package io.github.elettrone2012.quietschedule.platform.dnd

import android.service.notification.ZenPolicy
import androidx.test.filters.SdkSuppress
import io.github.elettrone2012.quietschedule.domain.model.ConversationCategory
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.SenderCategory
import io.github.elettrone2012.quietschedule.domain.model.SenderScope
import io.github.elettrone2012.quietschedule.domain.model.SuppressedVisualEffects
import org.junit.Assert.assertEquals
import org.junit.Test

@SdkSuppress(minSdkVersion = 35)
class ZenPolicyMapperTest {

    @Test
    fun disabledCallsMapToNone() {
        val policy = DndPolicy(
            calls = SenderCategory(
                enabled = false,
                sender = SenderScope.STARRED
            )
        )

        val zenPolicy = policy.toZenPolicy()

        assertEquals(
            ZenPolicy.PEOPLE_TYPE_NONE,
            zenPolicy.priorityCallSenders
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

        val zenPolicy = policy.toZenPolicy()

        assertEquals(
            ZenPolicy.PEOPLE_TYPE_CONTACTS,
            zenPolicy.priorityCallSenders
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

        val zenPolicy = policy.toZenPolicy()

        assertEquals(
            ZenPolicy.PEOPLE_TYPE_STARRED,
            zenPolicy.priorityMessageSenders
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

        val zenPolicy = policy.toZenPolicy()

        assertEquals(
            ZenPolicy.CONVERSATION_SENDERS_NONE,
            zenPolicy.priorityConversationSenders
        )
    }

    @Test
    fun enabledImportantConversationsMapCorrectly() {
        val policy = DndPolicy(
            conversations = ConversationCategory(
                enabled = true,
                sender = ConversationScope.IMPORTANT
            )
        )

        val zenPolicy = policy.toZenPolicy()

        assertEquals(
            ZenPolicy.CONVERSATION_SENDERS_IMPORTANT,
            zenPolicy.priorityConversationSenders
        )
    }

    @Test
    fun suppressedVisualEffectsAreInvertedToShowFlags() {
        val policy = DndPolicy(
            suppressedVisualEffects = SuppressedVisualEffects(
                fullScreenIntent = true,
                lights = true,
                peek = true,
                statusBar = true,
                badge = true,
                ambient = true,
                notificationList = true
            )
        )

        val zenPolicy = policy.toZenPolicy()

        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectFullScreenIntent
        )
        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectLights
        )
        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectPeek
        )
        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectStatusBar
        )
        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectBadge
        )
        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectAmbient
        )
        assertEquals(
            ZenPolicy.STATE_DISALLOW,
            zenPolicy.visualEffectNotificationList
        )
    }
}