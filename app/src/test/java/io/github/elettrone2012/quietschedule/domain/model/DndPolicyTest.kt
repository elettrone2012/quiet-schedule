package io.github.elettrone2012.quietschedule.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DndPolicyTest {

    @Test
    fun newPolicyHasAllPriorityCategoriesDisabled() {
        val policy = DndPolicy()

        assertFalse(policy.allowAlarms)
        assertFalse(policy.allowReminders)
        assertFalse(policy.allowEvents)
        assertFalse(policy.allowMedia)
        assertFalse(policy.allowSystem)
        assertFalse(policy.allowRepeatCallers)

        assertFalse(policy.calls.enabled)
        assertFalse(policy.messages.enabled)
        assertFalse(policy.conversations.enabled)
    }

    @Test
    fun newPolicyHasAllVisualSuppressionsDisabled() {
        val effects = DndPolicy().suppressedVisualEffects

        assertFalse(effects.fullScreenIntent)
        assertFalse(effects.lights)
        assertFalse(effects.peek)
        assertFalse(effects.statusBar)
        assertFalse(effects.badge)
        assertFalse(effects.ambient)
        assertFalse(effects.notificationList)
    }

    @Test
    fun senderScopesExposeOnlySupportedApi30Values() {
        val senderScopes = SenderScope.entries

        assertEquals(3, senderScopes.size)
        assertTrue(SenderScope.ANYONE in senderScopes)
        assertTrue(SenderScope.CONTACTS in senderScopes)
        assertTrue(SenderScope.STARRED in senderScopes)
    }

    @Test
    fun conversationScopesExposeOnlySupportedApi30Values() {
        val conversationScopes = ConversationScope.entries

        assertEquals(2, conversationScopes.size)
        assertTrue(ConversationScope.ANYONE in conversationScopes)
        assertTrue(ConversationScope.IMPORTANT in conversationScopes)
    }

    @Test
    fun disabledCallsCanKeepStoredSenderScope() {
        val calls = SenderCategory(
            enabled = false,
            sender = SenderScope.STARRED
        )

        assertFalse(calls.enabled)
        assertEquals(SenderScope.STARRED, calls.sender)
    }

    @Test
    fun disabledMessagesCanKeepStoredSenderScope() {
        val messages = SenderCategory(
            enabled = false,
            sender = SenderScope.CONTACTS
        )

        assertFalse(messages.enabled)
        assertEquals(SenderScope.CONTACTS, messages.sender)
    }

    @Test
    fun disabledConversationsCanKeepStoredSenderScope() {
        val conversations = ConversationCategory(
            enabled = false,
            sender = ConversationScope.IMPORTANT
        )

        assertFalse(conversations.enabled)
        assertEquals(ConversationScope.IMPORTANT, conversations.sender)
    }
}