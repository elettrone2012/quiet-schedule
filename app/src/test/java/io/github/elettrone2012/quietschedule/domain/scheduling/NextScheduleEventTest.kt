package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class NextScheduleEventTest {

    @Test
    fun pastEventOnSameDayMovesToNextWeek() {
        val profile = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            18,
            0
        )

        val event = findNextScheduleEvent(
            profiles = listOf(profile),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                17,
                9,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            ScheduleEventType.START,
            event?.type
        )
    }

    @Test
    fun nextEventLaterTodayIsReturned() {
        val profile = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            8,
            0
        )

        val event = findNextScheduleEvent(
            profiles = listOf(profile),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                10,
                9,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            ScheduleEventType.START,
            event?.type
        )
    }

    @Test
    fun activeScheduleReturnsEndAsNextEvent() {
        val profile = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            12,
            0
        )

        val event = findNextScheduleEvent(
            profiles = listOf(profile),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                10,
                17,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            ScheduleEventType.END,
            event?.type
        )
    }

    @Test
    fun earliestEventAcrossProfilesIsReturned() {
        val work = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val evening = Profile(
            name = "Evening",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 18 * 60,
                    endMinute = 22 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            17,
            30
        )

        val event = findNextScheduleEvent(
            profiles = listOf(work, evening),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                10,
                18,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            "Evening",
            event?.profile?.name
        )

        assertEquals(
            ScheduleEventType.START,
            event?.type
        )
    }

    @Test
    fun adjacentProfilesHaveSharedBoundaryAtSameTime() {
        val morning = Profile(
            name = "Morning",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 6 * 60,
                    endMinute = 8 * 60
                )
            )
        )

        val work = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 8 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            7,
            30
        )

        val event = findNextScheduleEvent(
            profiles = listOf(morning, work),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                10,
                8,
                0
            ),
            event?.dateTime
        )
    }

    @Test
    fun disabledProfilesAreIgnored() {
        val disabledProfile = Profile(
            name = "Disabled",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 10 * 60
                )
            )
        )

        val enabledProfile = Profile(
            name = "Enabled",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 11 * 60,
                    endMinute = 12 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            8,
            0
        )

        val event = findNextScheduleEvent(
            profiles = listOf(
                disabledProfile,
                enabledProfile
            ),
            now = now
        )

        assertEquals(
            "Enabled",
            event?.profile?.name
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                10,
                11,
                0
            ),
            event?.dateTime
        )
    }

    @Test
    fun nextEventCanBeOnFollowingDay() {
        val profile = Profile(
            name = "Tuesday",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            18,
            0
        )

        val event = findNextScheduleEvent(
            profiles = listOf(profile),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                11,
                9,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            ScheduleEventType.START,
            event?.type
        )
    }

    @Test
    fun scheduleEndingAtMidnightReturnsNextDayMidnightAsEndEvent() {
        val profile = Profile(
            name = "Evening",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 22 * 60,
                    endMinute = Schedule.MINUTES_PER_DAY
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            10,
            23,
            0
        )

        val event = findNextScheduleEvent(
            profiles = listOf(profile),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                11,
                0,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            ScheduleEventType.END,
            event?.type
        )
    }

    @Test
    fun midnightEndBoundaryMovesToNextWeekAfterItHasPassed() {
        val profile = Profile(
            name = "Evening",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 22 * 60,
                    endMinute = Schedule.MINUTES_PER_DAY
                )
            )
        )

        val now = LocalDateTime.of(
            2026,
            8,
            11,
            0,
            1
        )

        val event = findNextScheduleEvent(
            profiles = listOf(profile),
            now = now
        )

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                17,
                22,
                0
            ),
            event?.dateTime
        )

        assertEquals(
            ScheduleEventType.START,
            event?.type
        )
    }

    @Test
    fun emptyProfileListReturnsNull() {
        val now = LocalDateTime.of(
            2026,
            8,
            10,
            8,
            0
        )

        val event = findNextScheduleEvent(
            profiles = emptyList(),
            now = now
        )

        assertNull(event)
    }
}