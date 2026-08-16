package io.github.elettrone2012.quietschedule.domain.profile

import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class ProfileServiceTest {

    @Test
    fun enableProfileEnablesRequestedProfile() {
        val profile = Profile(
            id = "work",
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

        val result = enableProfile(
            profileId = "work",
            profiles = listOf(profile)
        )

        assertTrue(result is EnableProfileResult.Success)

        val updatedProfiles =
            (result as EnableProfileResult.Success).profiles

        assertTrue(updatedProfiles.first().enabled)
    }

    @Test
    fun enableProfileIsRejectedWhenConflictExists() {
        val work = Profile(
            id = "work",
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

        val morning = Profile(
            id = "morning",
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

        val result = enableProfile(
            profileId = "work",
            profiles = listOf(work, morning)
        )

        assertTrue(result is EnableProfileResult.Conflict)
    }

    @Test
    fun enableProfileReturnsNotFoundForUnknownId() {
        val result = enableProfile(
            profileId = "missing",
            profiles = emptyList()
        )

        assertTrue(result is EnableProfileResult.ProfileNotFound)
    }

    @Test
    fun enablingOneProfileDoesNotModifyOthers() {
        val work = Profile(
            id = "work",
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

        val evening = Profile(
            id = "evening",
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

        val result = enableProfile(
            profileId = "work",
            profiles = listOf(work, evening)
        )

        val updatedProfiles =
            (result as EnableProfileResult.Success).profiles

        val updatedWork =
            updatedProfiles.first { it.id == "work" }

        val updatedEvening =
            updatedProfiles.first { it.id == "evening" }

        assertTrue(updatedWork.enabled)
        assertTrue(updatedEvening.enabled)
        assertEquals("Evening", updatedEvening.name)
        assertFalse(updatedEvening.id == updatedWork.id)
    }

    @Test
    fun disableProfileDisablesRequestedProfile() {
        val profile = Profile(
            id = "work",
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

        val updatedProfiles = disableProfile(
            profileId = "work",
            profiles = listOf(profile)
        )

        assertFalse(updatedProfiles.first().enabled)
    }

    @Test
    fun disablingOneProfileDoesNotModifyOthers() {
        val work = Profile(
            id = "work",
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
            id = "evening",
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

        val updatedProfiles = disableProfile(
            profileId = "work",
            profiles = listOf(work, evening)
        )

        val updatedWork =
            updatedProfiles.first { it.id == "work" }

        val updatedEvening =
            updatedProfiles.first { it.id == "evening" }

        assertFalse(updatedWork.enabled)
        assertTrue(updatedEvening.enabled)
    }

    @Test
    fun duplicateProfileCopiesConfigurationAndStartsDisabled() {
        val source = Profile(
            id = "work",
            name = "Work",
            enabled = true,
            dndPolicy = DndPolicy(
                allowAlarms = true
            ),
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val updatedProfiles = duplicateProfile(
            profileId = "work",
            profiles = listOf(source),
            newProfileId = "work-copy"
        )

        val duplicate =
            updatedProfiles.first { it.id == "work-copy" }

        assertEquals("Work (copy)", duplicate.name)
        assertFalse(duplicate.enabled)
        assertEquals(source.dndPolicy, duplicate.dndPolicy)
        assertEquals(source.schedules, duplicate.schedules)
    }

    @Test
    fun duplicateProfileDoesNothingWhenSourceDoesNotExist() {
        val original = emptyList<Profile>()

        val updatedProfiles = duplicateProfile(
            profileId = "missing",
            profiles = original,
            newProfileId = "copy"
        )

        assertEquals(original, updatedProfiles)
    }

    @Test
    fun deleteProfileRemovesRequestedProfile() {
        val work = Profile(
            id = "work",
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
            id = "evening",
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

        val updatedProfiles = deleteProfile(
            profileId = "work",
            profiles = listOf(work, evening)
        )

        assertEquals(1, updatedProfiles.size)
        assertEquals("evening", updatedProfiles.first().id)
    }

    @Test
    fun deleteProfileDoesNothingWhenIdDoesNotExist() {
        val work = Profile(
            id = "work",
            name = "Work",
            enabled = false,
            schedules = emptyList()
        )

        val original = listOf(work)

        val updatedProfiles = deleteProfile(
            profileId = "missing",
            profiles = original
        )

        assertEquals(original, updatedProfiles)
    }

    @Test
    fun disabledProfileCanBeSavedWithConflict() {
        val original = Profile(
            id = "work",
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

        val other = Profile(
            id = "morning",
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

        val edited = original.copy(
            name = "Work edited"
        )

        val result = saveProfile(
            updatedProfile = edited,
            profiles = listOf(original, other)
        )

        assertTrue(result is SaveProfileResult.Success)
    }

    @Test
    fun enabledProfileCannotBeSavedWithConflict() {
        val original = Profile(
            id = "work",
            name = "Work",
            enabled = true,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 10 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val other = Profile(
            id = "morning",
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

        val edited = original.copy(
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 9 * 60,
                    endMinute = 17 * 60
                )
            )
        )

        val result = saveProfile(
            updatedProfile = edited,
            profiles = listOf(original, other)
        )

        assertTrue(result is SaveProfileResult.Conflict)
    }

    @Test
    fun enableProfileAcceptsAdjacentMidnightBoundary() {
        val mondayEvening = Profile(
            id = "evening",
            name = "Evening",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 22 * 60,
                    endMinute = Schedule.MINUTES_PER_DAY
                )
            )
        )

        val tuesdayMorning = Profile(
            id = "morning",
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

        val result = enableProfile(
            profileId = "evening",
            profiles = listOf(
                mondayEvening,
                tuesdayMorning
            )
        )

        assertTrue(
            result is EnableProfileResult.Success
        )
    }

    @Test
    fun enableProfileRejectsConflictBeforeMidnight() {
        val evening = Profile(
            id = "evening",
            name = "Evening",
            enabled = false,
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startMinute = 22 * 60,
                    endMinute = Schedule.MINUTES_PER_DAY
                )
            )
        )

        val late = Profile(
            id = "late",
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

        val result = enableProfile(
            profileId = "evening",
            profiles = listOf(
                evening,
                late
            )
        )

        assertTrue(
            result is EnableProfileResult.Conflict
        )
    }

    @Test
    fun saveProfileReturnsNotFoundForUnknownId() {
        val profile = Profile(
            id = "missing",
            name = "Missing",
            enabled = false,
            schedules = emptyList()
        )

        val result = saveProfile(
            updatedProfile = profile,
            profiles = emptyList()
        )

        assertTrue(
            result is SaveProfileResult.ProfileNotFound
        )
    }

    @Test
    fun disableAllProfilesTurnsEveryEnabledProfileOff() {
        val profiles = listOf(
            Profile(
                id = "work",
                name = "Work",
                enabled = true,
                schedules = emptyList()
            ),
            Profile(
                id = "evening",
                name = "Evening",
                enabled = true,
                schedules = emptyList()
            )
        )

        val updated =
            disableAllProfiles(profiles)

        assertTrue(
            updated.all { !it.enabled }
        )
    }

    @Test
    fun disableAllProfilesLeavesAlreadyDisabledProfilesUnchanged() {
        val profile = Profile(
            id = "work",
            name = "Work",
            enabled = false,
            schedules = emptyList()
        )

        val updated =
            disableAllProfiles(
                listOf(profile)
            )

        assertEquals(
            listOf(profile),
            updated
        )
    }
}