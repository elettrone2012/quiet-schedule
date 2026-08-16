package io.github.elettrone2012.quietschedule.domain.conflict

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ProfileConflictTest {

    @Test
    fun enabledOverlappingProfileProducesConflict() {
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

        val otherProfile = Profile(
            name = "Morning",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 8 * 60,
                    endMinute = 10 * 60
                )
            )
        )

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNotNull(conflict)
        assertEquals(
            "Morning",
            conflict?.conflictingProfile?.name
        )
        assertEquals(
            DayOfWeek.MONDAY,
            conflict?.scheduleConflict?.dayOfWeek
        )
        assertEquals(
            LocalTime.of(9, 0),
            conflict?.scheduleConflict?.overlapStart
        )
        assertEquals(
            LocalTime.of(10, 0),
            conflict?.scheduleConflict?.overlapEnd
        )
    }

    @Test
    fun disabledOtherProfileIsIgnored() {
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

        val otherProfile = Profile(
            name = "Morning",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 8 * 60,
                    endMinute = 10 * 60
                )
            )
        )

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNull(conflict)
    }

    @Test
    fun sameTimeOnDifferentDaysDoesNotConflict() {
        val profile = Profile(
            name = "Work Monday",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val otherProfile = Profile(
            name = "Work Tuesday",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNull(conflict)
    }

    @Test
    fun conflictIsDetectedOnOneOfMultipleSelectedDays() {
        val profile = Profile(
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY
                    ),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val otherProfile = Profile(
            name = "Tuesday meeting",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startMinute = 16 * 60,
                    endMinute = 18 * 60
                )
            )
        )

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNotNull(conflict)

        assertEquals(
            DayOfWeek.TUESDAY,
            conflict?.scheduleConflict?.dayOfWeek
        )

        assertEquals(
            LocalTime.of(16, 0),
            conflict?.scheduleConflict?.overlapStart
        )

        assertEquals(
            LocalTime.of(17, 0),
            conflict?.scheduleConflict?.overlapEnd
        )
    }

    @Test
    fun adjacentProfilesDoNotConflict() {
        val profile = Profile(
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

        val otherProfile = Profile(
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

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNull(conflict)
    }

    @Test
    fun scheduleEndingAtMidnightConflictsWithLateEveningSchedule() {
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

        val otherProfile = Profile(
            name = "Late",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 23 * 60,
                    endMinute = 23 * 60 + 30
                )
            )
        )

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNotNull(conflict)

        assertEquals(
            LocalTime.of(23, 0),
            conflict?.scheduleConflict?.overlapStart
        )

        assertEquals(
            LocalTime.of(23, 30),
            conflict?.scheduleConflict?.overlapEnd
        )
    }
}