package io.github.elettrone2012.quietschedule.domain.validation

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class ProfileValidationTest {

    @Test
    fun enableIsRejectedWhenConflictExists() {
        val profile = Profile(
            name = "Work",
            enabled = false,
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

        val result = validateProfileForEnable(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(result is ProfileValidationResult.Conflict)
    }

    @Test
    fun enableIsAcceptedWhenNoConflictExists() {
        val profile = Profile(
            name = "Work",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
                )
            )
        )

        val otherProfile = Profile(
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

        val result = validateProfileForEnable(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(result is ProfileValidationResult.Valid)
    }

    @Test
    fun disabledProfileCanBeSavedEvenWithConflict() {
        val profile = Profile(
            name = "Work",
            enabled = false,
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

        val result = validateProfileForSave(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(result is ProfileValidationResult.Valid)
    }

    @Test
    fun enabledProfileCannotBeSavedWithConflict() {
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

        val result = validateProfileForSave(
            profile = profile,
            otherProfiles = listOf(otherProfile)
        )

        assertTrue(result is ProfileValidationResult.Conflict)
    }

}