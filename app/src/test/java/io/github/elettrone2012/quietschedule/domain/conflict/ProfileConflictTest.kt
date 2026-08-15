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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
                )
            )
        )

        val otherProfile = Profile(
            name = "Morning",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(10, 0)
                )
            )
        )

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNotNull(conflict)
        assertEquals("Morning", conflict?.conflictingProfile?.name)
        assertEquals(DayOfWeek.MONDAY, conflict?.scheduleConflict?.dayOfWeek)
        assertEquals(LocalTime.of(9, 0), conflict?.scheduleConflict?.overlapStart)
        assertEquals(LocalTime.of(10, 0), conflict?.scheduleConflict?.overlapEnd)
    }

    @Test
    fun disabledOtherProfileIsIgnored() {
        val profile = Profile(
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

        val otherProfile = Profile(
            name = "Morning",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(10, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
                )
            )
        )

        val otherProfile = Profile(
            name = "Work Tuesday",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
                )
            )
        )

        val otherProfile = Profile(
            name = "Tuesday meeting",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startTime = LocalTime.of(16, 0),
                    endTime = LocalTime.of(18, 0)
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
                    startTime = LocalTime.of(6, 0),
                    endTime = LocalTime.of(8, 0)
                )
            )
        )

        val otherProfile = Profile(
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

        val conflict = findConflict(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertNull(conflict)
    }
}