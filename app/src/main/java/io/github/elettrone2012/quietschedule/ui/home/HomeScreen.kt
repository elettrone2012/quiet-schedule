package io.github.elettrone2012.quietschedule.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elettrone2012.quietschedule.R
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profiles: List<Profile>,
    errorMessage: String?,
    dndAccessGranted: Boolean,
    onRequestDndAccess: () -> Unit,
    onNewProfile: () -> Unit,
    onProfileClick: (Profile) -> Unit,
    onProfileEnabledChange: (
        profile: Profile,
        enabled: Boolean
    ) -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val versionName =
        remember(context) {
            context.packageManager
                .getPackageInfo(
                    context.packageName,
                    0
                )
                .versionName
                ?: ""
        }

    val locale =
        if (configuration.locales.isEmpty) {
            Locale.getDefault()
        } else {
            configuration.locales[0]
        }

    val use24HourFormat =
        DateFormat.is24HourFormat(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(
                                R.string.app_name
                            )
                        )

                        Text(
                            text = stringResource(
                                R.string.app_version,
                                versionName
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.profiles
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            if (!dndAccessGranted) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.dnd_access_required
                        ),
                        color =
                            MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick =
                            onRequestDndAccess,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(
                                R.string.grant_dnd_access
                            )
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            if (profiles.isEmpty()) {
                Text(
                    stringResource(
                        R.string.no_profiles_configured
                    )
                )
            } else {
                profiles.forEach { profile ->
                    ProfileRow(
                        profile = profile,
                        locale = locale,
                        use24HourFormat =
                            use24HourFormat,
                        onClick = {
                            onProfileClick(
                                profile
                            )
                        },
                        onEnabledChange = {
                                enabled ->

                            onProfileEnabledChange(
                                profile,
                                enabled
                            )
                        }
                    )
                }
            }

            Button(
                onClick = onNewProfile,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.new_profile
                    )
                )
            }

            Button(
                onClick = onSettings,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.settings
                    )
                )
            }

            Button(
                onClick = onHelp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.help
                    )
                )
            }
        }
    }
}

@Composable
private fun ProfileRow(
    profile: Profile,
    locale: Locale,
    use24HourFormat: Boolean,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                vertical = 8.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = profile.name,
            style =
                MaterialTheme.typography.titleMedium
        )

        profile.schedules.forEach { schedule ->
            Text(
                text = formatSchedule(
                    schedule = schedule,
                    locale = locale,
                    use24HourFormat =
                        use24HourFormat
                ),
                style =
                    MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Switch(
                checked =
                    profile.enabled,
                onCheckedChange =
                    onEnabledChange
            )
        }
    }
}

private fun formatSchedule(
    schedule: Schedule,
    locale: Locale,
    use24HourFormat: Boolean
): String {
    val days =
        formatDays(
            days = schedule.daysOfWeek,
            locale = locale
        )

    val start =
        formatTime(
            time = schedule.startTime,
            locale = locale,
            use24HourFormat =
                use24HourFormat
        )

    val end =
        formatTime(
            time = schedule.endTime,
            locale = locale,
            use24HourFormat =
                use24HourFormat
        )

    return "$days $start → $end"
}

private fun formatTime(
    time: LocalTime,
    locale: Locale,
    use24HourFormat: Boolean
): String {
    val pattern =
        if (use24HourFormat) {
            "HH:mm"
        } else {
            "h:mm a"
        }

    return time.format(
        DateTimeFormatter.ofPattern(
            pattern,
            locale
        )
    )
}

private fun formatDays(
    days: Set<DayOfWeek>,
    locale: Locale
): String {
    if (days.isEmpty()) {
        return ""
    }

    val orderedDays =
        DayOfWeek.entries.filter {
            it in days
        }

    fun shortName(
        day: DayOfWeek
    ): String {
        return day.getDisplayName(
            TextStyle.SHORT,
            locale
        )
    }

    val result =
        mutableListOf<String>()

    var rangeStart = 0

    while (
        rangeStart <
        orderedDays.size
    ) {
        var rangeEnd =
            rangeStart

        while (
            rangeEnd + 1 <
            orderedDays.size &&
            orderedDays[rangeEnd + 1].value ==
            orderedDays[rangeEnd].value + 1
        ) {
            rangeEnd++
        }

        result +=
            if (
                rangeStart ==
                rangeEnd
            ) {
                shortName(
                    orderedDays[rangeStart]
                )
            } else {
                "${shortName(orderedDays[rangeStart])}-" +
                        shortName(
                            orderedDays[rangeEnd]
                        )
            }

        rangeStart =
            rangeEnd + 1
    }

    return result.joinToString(", ")
}