package io.github.elettrone2012.quietschedule.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.quietScheduleDataStore by preferencesDataStore(
    name = "quietschedule"
)