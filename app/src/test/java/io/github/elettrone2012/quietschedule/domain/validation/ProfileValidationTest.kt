package io.github.elettrone2012.quietschedule.domain.validation

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class ProfileValidationTest {

    @Test
    fun enableIsRejectedWhenConflictExists() {
        val profile = Profile(
            name = "Work",
            enabled = false,
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

        val result = validateProfileForEnable(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(
            result is ProfileValidationResult.Conflict
        )
    }

    @Test
    fun enableIsAcceptedWhenNoConflictExists() {
        val profile = Profile(
            name = "Work",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val otherProfile = Profile(
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

        val result = validateProfileForEnable(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(
            result is ProfileValidationResult.Valid
        )
    }

    @Test
    fun disabledProfileCanBeSavedEvenWithConflict() {
        val profile = Profile(
            name = "Work",
            enabled = false,
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

        val result = validateProfileForSave(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(
            result is ProfileValidationResult.Valid
        )
    }

    @Test
    fun enabledProfileCannotBeSavedWithConflict() {
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

        val result = validateProfileForSave(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(
            result is ProfileValidationResult.Conflict
        )
    }

    @Test
    fun midnightBoundaryOnDifferentDaysDoesNotConflict() {
        val profile = Profile(
            name = "Monday evening",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 22 * 60,
                    endMinute = Schedule.MINUTES_PER_DAY
                )
            )
        )

        val otherProfile = Profile(
            name = "Tuesday morning",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.TUESDAY),
                    startMinute = 0,
                    endMinute = 8 * 60
                )
            )
        )

        val result = validateProfileForEnable(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(
            result is ProfileValidationResult.Valid
        )
    }
}