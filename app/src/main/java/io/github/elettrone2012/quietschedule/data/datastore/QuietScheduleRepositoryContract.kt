package io.github.elettrone2012.quietschedule.data.datastore

import io.github.elettrone2012.quietschedule.domain.model.AppSettings
import io.github.elettrone2012.quietschedule.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface QuietScheduleRepositoryContract {

    val profiles: Flow<List<Profile>>

    val appSettings: Flow<AppSettings>

    suspend fun saveProfiles(
        profiles: List<Profile>
    )

    suspend fun setShowStatusNotification(
        enabled: Boolean
    )
}