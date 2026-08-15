package io.github.elettrone2012.quietschedule.domain.validation

import io.github.elettrone2012.quietschedule.domain.conflict.ProfileConflict
import io.github.elettrone2012.quietschedule.domain.conflict.findConflict
import io.github.elettrone2012.quietschedule.domain.model.Profile

sealed interface ProfileValidationResult {
    data object Valid : ProfileValidationResult

    data class Conflict(
        val details: ProfileConflict
    ) : ProfileValidationResult
}

fun validateProfileForEnable(
    profile: Profile,
    otherProfiles: List<Profile>
): ProfileValidationResult {
    val conflict = findConflict(
        profile = profile,
        otherProfiles = otherProfiles
    )

    return if (conflict == null) {
        ProfileValidationResult.Valid
    } else {
        ProfileValidationResult.Conflict(conflict)
    }
}

fun validateProfileForSave(
    profile: Profile,
    otherProfiles: List<Profile>
): ProfileValidationResult {
    if (!profile.enabled) {
        return ProfileValidationResult.Valid
    }

    return validateProfileForEnable(
        profile = profile,
        otherProfiles = otherProfiles
    )
}