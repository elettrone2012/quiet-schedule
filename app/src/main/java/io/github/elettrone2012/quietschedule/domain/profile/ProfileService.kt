package io.github.elettrone2012.quietschedule.domain.profile

import io.github.elettrone2012.quietschedule.domain.conflict.ProfileConflict
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.validation.ProfileValidationResult
import io.github.elettrone2012.quietschedule.domain.validation.validateProfileForEnable
import io.github.elettrone2012.quietschedule.domain.validation.validateProfileForSave

sealed interface EnableProfileResult {

    data class Success(
        val profiles: List<Profile>
    ) : EnableProfileResult

    data class Conflict(
        val details: ProfileConflict
    ) : EnableProfileResult

    data object ProfileNotFound : EnableProfileResult
}

fun enableProfile(
    profileId: String,
    profiles: List<Profile>
): EnableProfileResult {
    val profile = profiles.firstOrNull { it.id == profileId }
        ?: return EnableProfileResult.ProfileNotFound

    val otherProfiles = profiles.filter { it.id != profileId }

    return when (
        val validation = validateProfileForEnable(
            profile = profile,
            otherProfiles = otherProfiles
        )
    ) {
        ProfileValidationResult.Valid -> {
            EnableProfileResult.Success(
                profiles = profiles.map {
                    if (it.id == profileId) {
                        it.copy(enabled = true)
                    } else {
                        it
                    }
                }
            )
        }

        is ProfileValidationResult.Conflict -> {
            EnableProfileResult.Conflict(
                details = validation.details
            )
        }
    }
}

fun disableProfile(
    profileId: String,
    profiles: List<Profile>
): List<Profile> {
    return profiles.map { profile ->
        if (profile.id == profileId) {
            profile.copy(enabled = false)
        } else {
            profile
        }
    }
}

fun duplicateProfile(
    profileId: String,
    profiles: List<Profile>,
    newProfileId: String
): List<Profile> {
    val source = profiles.firstOrNull { it.id == profileId }
        ?: return profiles

    val duplicate = source.copy(
        id = newProfileId,
        name = "${source.name} (copy)",
        enabled = false
    )

    return profiles + duplicate
}

fun deleteProfile(
    profileId: String,
    profiles: List<Profile>
): List<Profile> {
    return profiles.filterNot { it.id == profileId }
}

sealed interface SaveProfileResult {

    data class Success(
        val profiles: List<Profile>
    ) : SaveProfileResult

    data class Conflict(
        val details: ProfileConflict
    ) : SaveProfileResult

    data object ProfileNotFound : SaveProfileResult
}

fun saveProfile(
    updatedProfile: Profile,
    profiles: List<Profile>
): SaveProfileResult {
    val existing = profiles.firstOrNull { it.id == updatedProfile.id }
        ?: return SaveProfileResult.ProfileNotFound

    val otherProfiles = profiles.filter { it.id != updatedProfile.id }

    return when (
        val validation = validateProfileForSave(
            profile = updatedProfile,
            otherProfiles = otherProfiles
        )
    ) {
        ProfileValidationResult.Valid -> {
            SaveProfileResult.Success(
                profiles = profiles.map {
                    if (it.id == existing.id) {
                        updatedProfile
                    } else {
                        it
                    }
                }
            )
        }

        is ProfileValidationResult.Conflict -> {
            SaveProfileResult.Conflict(
                details = validation.details
            )
        }
    }
}

fun disableAllProfiles(
    profiles: List<Profile>
): List<Profile> {
    return profiles.map { profile ->
        if (profile.enabled) {
            profile.copy(enabled = false)
        } else {
            profile
        }
    }
}


