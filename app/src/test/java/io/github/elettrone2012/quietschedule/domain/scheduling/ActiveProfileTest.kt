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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
                )
            )
        )

        val evening = Profile(
            name = "Evening",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(18, 0),
                    endTime = LocalTime.of(22, 0)
                )
            )
        )

        val activeProfile = findActiveProfile(
            profiles = listOf(work, evening),
            dayOfWeek = DayOfWeek.MONDAY,
            time = LocalTime.of(12, 0)
        )

        assertEquals("Work", activeProfile?.name)
    }

    @Test
    fun returnsNullWhenNoProfileIsActive() {
        val work = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(6, 0),
                    endTime = LocalTime.of(8, 0)
                )
            )
        )

        val second = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(17, 0)
                )
            )
        )

        val activeProfile = findActiveProfile(
            profiles = listOf(first, second),
            dayOfWeek = DayOfWeek.MONDAY,
            time = LocalTime.of(8, 0)
        )

        assertEquals("Work", activeProfile?.name)
    }


}