package io.github.elettrone2012.quietschedule.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.elettrone2012.quietschedule.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val PROFILES =
    stringPreferencesKey("profiles")

private val json = Json {
    ignoreUnknownKeys = true
}

fun Context.observeProfiles(): Flow<List<Profile>> {
    return quietScheduleDataStore.data.map { preferences ->
        val serialized = preferences[PROFILES]

        if (serialized.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching {
                json.decodeFromString<List<PersistedProfile>>(serialized)
            }.getOrElse {
                emptyList()
            }.mapNotNull { persistedProfile ->
                runCatching {
                    persistedProfile.toDomain()
                }.getOrNull()
            }
        }
    }
}

suspend fun Context.saveProfiles(
    profiles: List<Profile>
) {
    val serialized = json.encodeToString(
        profiles.map { it.toPersisted() }
    )

    quietScheduleDataStore.edit { preferences ->
        preferences[PROFILES] = serialized
    }
}