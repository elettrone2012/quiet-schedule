package io.github.elettrone2012.quietschedule.domain.conflict

import io.github.elettrone2012.quietschedule.domain.model.Profile

data class ProfileConflict(
    val conflictingProfile: Profile,
    val scheduleConflict: ScheduleConflict
)

fun findConflict(
    profile: Profile,
    otherProfiles: List<Profile>
): ProfileConflict? {
    for (otherProfile in otherProfiles) {
        if (!otherProfile.enabled) {
            continue
        }

        for (schedule in profile.schedules) {
            for (otherSchedule in otherProfile.schedules) {
                val conflict = findConflict(schedule, otherSchedule)

                if (conflict != null) {
                    return ProfileConflict(
                        conflictingProfile = otherProfile,
                        scheduleConflict = conflict
                    )
                }
            }
        }
    }

    return null
}