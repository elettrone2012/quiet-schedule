package io.github.elettrone2012.quietschedule.data.datastore

import android.content.Context
import io.github.elettrone2012.quietschedule.domain.model.AppSettings
import io.github.elettrone2012.quietschedule.domain.model.Profile
import kotlinx.coroutines.flow.Flow

class QuietScheduleRepository(
    private val context: Context
) : QuietScheduleRepositoryContract {

    override val profiles: Flow<List<Profile>>
        get() = context.observeProfiles()

    override val appSettings: Flow<AppSettings>
        get() = context.observeAppSettings()

    override suspend fun saveProfiles(
        profiles: List<Profile>
    ) {
        context.saveProfiles(profiles)
    }

    override suspend fun setShowStatusNotification(
        enabled: Boolean
    ) {
        context.setShowStatusNotification(enabled)
    }
}