package io.github.elettrone2012.quietschedule.domain.profile

import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy

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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(10, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(18, 0),
                    endTime = LocalTime.of(22, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(18, 0),
                    endTime = LocalTime.of(22, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(18, 0),
                    endTime = LocalTime.of(22, 0)
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
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(10, 0)
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
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(17, 0)
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
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(10, 0)
                )
            )
        )

        val edited = original.copy(
            schedules = listOf(
                Schedule(
                    daysOfWeek = setOf(DayOfWeek.MONDAY),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0)
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

        assertTrue(result is SaveProfileResult.ProfileNotFound)
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

        val updated = disableAllProfiles(profiles)

        assertTrue(updated.all { !it.enabled })
    }

    @Test
    fun disableAllProfilesLeavesAlreadyDisabledProfilesUnchanged() {
        val profile = Profile(
            id = "work",
            name = "Work",
            enabled = false,
            schedules = emptyList()
        )

        val updated = disableAllProfiles(listOf(profile))

        assertEquals(listOf(profile), updated)
    }



}