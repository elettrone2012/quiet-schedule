package io.github.elettrone2012.quietschedule.domain.scheduling

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ActiveProfileTest {

    @Test
    fun returnsActiveProfile() {
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

        val activeProfile = findActiveProfile(
            profiles = listOf(work, evening),
            dayOfWeek = DayOfWeek.MONDAY,
            time = LocalTime.of(12, 0)
        )

        assertEquals(
            "Work",
            activeProfile?.name
        )
    }

    @Test
    fun returnsNullWhenNoProfileIsActive() {
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

        val activeProfile = findActiveProfile(
            profiles = listOf(work),
            dayOfWeek = DayOfWeek.MONDAY,
            time = LocalTime.of(18, 0)
        )

        assertNull(activeProfile)
    }

    @Test
    fun adjacentProfilesSwitchAtSharedBoundary() {
        val first = Profile(
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

        val second = Profile(
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

        val activeProfile = findActiveProfile(
            profiles = listOf(first, second),
            dayOfWeek = DayOfWeek.MONDAY,
            time = LocalTime.of(8, 0)
        )

        assertEquals(
            "Work",
            activeProfile?.name
        )
    }

    @Test
    fun profileEndingAtMidnightIsActiveBeforeMidnight() {
        val evening = Profile(
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

        val activeProfile = findActiveProfile(
            profiles = listOf(evening),
            dayOfWeek = DayOfWeek.MONDAY,
            time = LocalTime.of(23, 59)
        )

        assertEquals(
            "Evening",
            activeProfile?.name
        )
    }

    @Test
    fun profileEndingAtMidnightIsNotActiveAtNextDayMidnight() {
        val evening = Profile(
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

        val activeProfile = findActiveProfile(
            profiles = listOf(evening),
            dayOfWeek = DayOfWeek.TUESDAY,
            time = LocalTime.MIDNIGHT
        )

        assertNull(activeProfile)
    }

    @Test
    fun midnightMorningScheduleIsActiveAtMidnight() {
        val morning = Profile(
            name = "Morning",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startMinute = 0,
                    endMinute = 8 * 60
                )
            )
        )

        val activeProfile = findActiveProfile(
            profiles = listOf(morning),
            dayOfWeek = DayOfWeek.TUESDAY,
            time = LocalTime.MIDNIGHT
        )

        assertEquals(
            "Morning",
            activeProfile?.name
        )
    }
}