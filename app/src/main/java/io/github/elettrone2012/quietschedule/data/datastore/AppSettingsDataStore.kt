package io.github.elettrone2012.quietschedule.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.github.elettrone2012.quietschedule.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val SHOW_STATUS_NOTIFICATION =
    booleanPreferencesKey("show_status_notification")

fun Context.observeAppSettings(): Flow<AppSettings> {
    return quietScheduleDataStore.data.map { preferences ->
        AppSettings(
            showStatusNotification =
                preferences[SHOW_STATUS_NOTIFICATION] ?: false
        )
    }
}

suspend fun Context.setShowStatusNotification(
    enabled: Boolean
) {
    quietScheduleDataStore.edit { preferences ->
        preferences[SHOW_STATUS_NOTIFICATION] = enabled
    }
}