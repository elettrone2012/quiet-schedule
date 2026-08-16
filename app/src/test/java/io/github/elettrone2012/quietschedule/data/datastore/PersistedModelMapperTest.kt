package io.github.elettrone2012.quietschedule.data.datastore

import io.github.elettrone2012.quietschedule.domain.model.ConversationCategory
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import io.github.elettrone2012.quietschedule.domain.model.SenderCategory
import io.github.elettrone2012.quietschedule.domain.model.SenderScope
import io.github.elettrone2012.quietschedule.domain.model.SuppressedVisualEffects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class PersistedModelMapperTest {

    @Test
    fun profileRoundTripPreservesAllData() {
        val original = Profile(
            id = "profile-1",
            name = "Work",
            enabled = true,
            dndPolicy = DndPolicy(
                allowAlarms = true,
                allowReminders = true,
                allowEvents = true,
                allowMedia = true,
                allowSystem = true,
                allowRepeatCallers = true,
                calls = SenderCategory(
                    enabled = true,
                    sender = SenderScope.STARRED
                ),
                messages = SenderCategory(
                    enabled = true,
                    sender = SenderScope.CONTACTS
                ),
                conversations = ConversationCategory(
                    enabled = true,
                    sender = ConversationScope.IMPORTANT
                ),
                suppressedVisualEffects = SuppressedVisualEffects(
                    fullScreenIntent = true,
                    lights = true,
                    peek = true,
                    statusBar = true,
                    badge = true,
                    ambient = true,
                    notificationList = true
                )
            ),
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY
                    ),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60 + 30
                )
            )
        )

        val restored = original
            .toPersisted()
            .toDomain()

        assertEquals(original, restored)
    }

    @Test
    fun multipleSchedulesRoundTripCorrectly() {
        val original = Profile(
            id = "profile-2",
            name = "Split shift",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(
                        DayOfWeek.MONDAY
                    ),
                    startMinute = 8 * 60,
                    endMinute = 12 * 60
                ),
                Schedule(
                    daysOfWeek = setOf(
                        DayOfWeek.MONDAY
                    ),
                    startMinute = 13 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val restored = original
            .toPersisted()
            .toDomain()

        assertEquals(original, restored)
    }

    @Test
    fun disabledSenderCategoriesPreserveStoredScope() {
        val original = Profile(
            id = "profile-3",
            name = "Stored scopes",
            enabled = false,
            dndPolicy = DndPolicy(
                calls = SenderCategory(
                    enabled = false,
                    sender = SenderScope.STARRED
                ),
                messages = SenderCategory(
                    enabled = false,
                    sender = SenderScope.CONTACTS
                ),
                conversations = ConversationCategory(
                    enabled = false,
                    sender = ConversationScope.IMPORTANT
                )
            ),
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(
                        DayOfWeek.FRIDAY
                    ),
                    startMinute = 18 * 60,
                    endMinute = 22 * 60
                )
            )
        )

        val restored = original
            .toPersisted()
            .toDomain()

        assertEquals(
            SenderScope.STARRED,
            restored.dndPolicy.calls.sender
        )

        assertEquals(
            SenderScope.CONTACTS,
            restored.dndPolicy.messages.sender
        )

        assertEquals(
            ConversationScope.IMPORTANT,
            restored.dndPolicy.conversations.sender
        )
    }

    @Test
    fun midnightEndRoundTripPreserves1440() {
        val original = Profile(
            id = "midnight",
            name = "Evening",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(
                        DayOfWeek.MONDAY
                    ),
                    startMinute = 22 * 60,
                    endMinute = Schedule.MINUTES_PER_DAY
                )
            )
        )

        val persisted =
            original.toPersisted()

        val persistedSchedule =
            persisted.schedules.single()

        assertEquals(
            22 * 60,
            persistedSchedule.startMinute
        )

        assertEquals(
            Schedule.MINUTES_PER_DAY,
            persistedSchedule.endMinute
        )

        val restored =
            persisted.toDomain()

        assertEquals(
            original,
            restored
        )
    }

    @Test
    fun legacyStringTimesAreMigratedToMinuteModel() {
        val persisted = PersistedProfile(
            id = "legacy",
            name = "Legacy",
            enabled = false,
            dndPolicy = PersistedDndPolicy(),
            schedules = listOf(
                PersistedSchedule(
                    daysOfWeek =
                        listOf("MONDAY"),
                    startTime =
                        "09:00",
                    endTime =
                        "17:30"
                )
            )
        )

        val restored =
            persisted.toDomain()

        val schedule =
            restored.schedules.single()

        assertEquals(
            9 * 60,
            schedule.startMinute
        )

        assertEquals(
            17 * 60 + 30,
            schedule.endMinute
        )
    }

    @Test
    fun invalidDayOfWeekIsRejected() {
        val persisted = PersistedProfile(
            id = "broken-day",
            name = "Broken",
            enabled = false,
            dndPolicy = PersistedDndPolicy(),
            schedules = listOf(
                PersistedSchedule(
                    daysOfWeek =
                        listOf("NOT_A_DAY"),
                    startMinute =
                        9 * 60,
                    endMinute =
                        17 * 60
                )
            )
        )

        val result = runCatching {
            persisted.toDomain()
        }

        assertTrue(
            result.isFailure
        )
    }

    @Test
    fun invalidLegacyTimeIsRejected() {
        val persisted = PersistedProfile(
            id = "broken-time",
            name = "Broken",
            enabled = false,
            dndPolicy = PersistedDndPolicy(),
            schedules = listOf(
                PersistedSchedule(
                    daysOfWeek =
                        listOf("MONDAY"),
                    startTime =
                        "INVALID",
                    endTime =
                        "17:00"
                )
            )
        )

        val result = runCatching {
            persisted.toDomain()
        }

        assertTrue(
            result.isFailure
        )
    }

    @Test
    fun overnightLegacyPersistedScheduleIsRejectedByDomainInvariant() {
        val persisted = PersistedProfile(
            id = "overnight",
            name = "Broken",
            enabled = false,
            dndPolicy = PersistedDndPolicy(),
            schedules = listOf(
                PersistedSchedule(
                    daysOfWeek =
                        listOf("MONDAY"),
                    startTime =
                        "22:00",
                    endTime =
                        "07:00"
                )
            )
        )

        val result = runCatching {
            persisted.toDomain()
        }

        assertTrue(
            result.isFailure
        )
    }

    @Test
    fun invalidNewFormatRangeIsRejectedByDomainInvariant() {
        val persisted = PersistedProfile(
            id = "invalid-new-format",
            name = "Broken",
            enabled = false,
            dndPolicy = PersistedDndPolicy(),
            schedules = listOf(
                PersistedSchedule(
                    daysOfWeek =
                        listOf("MONDAY"),
                    startMinute =
                        22 * 60,
                    endMinute =
                        7 * 60
                )
            )
        )

        val result = runCatching {
            persisted.toDomain()
        }

        assertTrue(
            result.isFailure
        )
    }
}