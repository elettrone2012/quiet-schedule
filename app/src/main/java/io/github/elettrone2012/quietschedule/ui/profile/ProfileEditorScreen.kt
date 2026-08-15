package io.github.elettrone2012.quietschedule.ui.profile

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elettrone2012.quietschedule.R
import io.github.elettrone2012.quietschedule.domain.conflict.ScheduleConflict
import io.github.elettrone2012.quietschedule.domain.conflict.findConflict
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private data class InternalScheduleConflict(
    val firstIndex: Int,
    val secondIndex: Int,
    val conflict: ScheduleConflict
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    isEditing: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    schedules: List<Schedule>,
    dndPolicy: DndPolicy,
    originalName: String,
    originalSchedules: List<Schedule>,
    originalDndPolicy: DndPolicy,
    errorMessage: String? = null,
    onBack: () -> Unit,
    onEditSchedule: (
        schedule: Schedule?,
        index: Int?
    ) -> Unit,
    onEditDndPolicy: () -> Unit,
    onSave: (
        name: String,
        schedules: List<Schedule>,
        dndPolicy: DndPolicy
    ) -> Unit,
    onDuplicateProfile: (() -> Unit)? = null,
    onDeleteProfile: (() -> Unit)? = null
) {
    val context =
        LocalContext.current

    val configuration =
        LocalConfiguration.current

    val locale =
        if (configuration.locales.isEmpty) {
            Locale.getDefault()
        } else {
            configuration.locales[0]
        }

    val use24HourFormat =
        DateFormat.is24HourFormat(context)

    var localErrorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var internalConflict by remember {
        mutableStateOf<InternalScheduleConflict?>(null)
    }

    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    var showDiscardConfirmation by remember {
        mutableStateOf(false)
    }

    /*
     * Per un nuovo profilo la baseline è:
     * - nome vuoto
     * - nessuna fascia
     * - DND predefinito
     *
     * Per un profilo esistente viene confrontato
     * lo stato temporaneo con la fotografia originale
     * ricevuta da MainActivity.
     */
    val hasUnsavedChanges =
        if (!isEditing) {
            name.isNotBlank() ||
                    schedules.isNotEmpty() ||
                    dndPolicy != DndPolicy()
        } else {
            name.trim() != originalName.trim() ||
                    schedules != originalSchedules ||
                    dndPolicy != originalDndPolicy
        }

    val addAtLeastOneScheduleMessage =
        stringResource(
            R.string.add_at_least_one_schedule
        )

    val internalConflictMessage =
        internalConflict?.let { details ->
            val localizedDay =
                details.conflict.dayOfWeek.getDisplayName(
                    TextStyle.FULL,
                    locale
                )

            stringResource(
                R.string.schedule_conflict,
                details.firstIndex + 1,
                details.secondIndex + 1,
                localizedDay,
                formatTime(
                    details.conflict.overlapStart,
                    locale,
                    use24HourFormat
                ),
                formatTime(
                    details.conflict.overlapEnd,
                    locale,
                    use24HourFormat
                )
            )
        }

    fun requestBack() {
        if (hasUnsavedChanges) {
            showDiscardConfirmation = true
        } else {
            onBack()
        }
    }

    /*
     * Intercetta il Back Android e la gesture Back.
     */
    BackHandler {
        requestBack()
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text(
                    stringResource(
                        R.string.delete_profile_question
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.delete_profile_message
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteProfile?.invoke()
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.delete
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.cancel
                        )
                    )
                }
            }
        )
    }

    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDiscardConfirmation = false
            },
            title = {
                Text(
                    stringResource(
                        R.string.unsaved_changes_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.unsaved_changes_message
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirmation = false
                        onBack()
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.discard_changes
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirmation = false
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.continue_editing
                        )
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditing) {
                                R.string.edit_profile
                            } else {
                                R.string.new_profile
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            requestBack()
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription =
                                stringResource(
                                    R.string.back
                                )
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
                Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = {
                    Text(
                        stringResource(
                            R.string.profile_name
                        )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            localErrorMessage?.let {
                Text(
                    text = it,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            internalConflictMessage?.let {
                Text(
                    text = it,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = stringResource(
                    R.string.schedules
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            if (schedules.isEmpty()) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(
                            R.string.no_schedules_configured
                        )
                    )

                    if (isEditing) {
                        Text(
                            text =
                                stringResource(
                                    R.string.add_schedule_before_saving
                                ),
                            color =
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                schedules.forEachIndexed { index, schedule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onEditSchedule(
                                    schedule,
                                    index
                                )
                            }
                            .padding(
                                vertical = 12.dp
                            )
                    ) {
                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {
                            Text(
                                text =
                                    formatDays(
                                        schedule.daysOfWeek,
                                        locale
                                    )
                            )

                            Text(
                                text =
                                    "${
                                        formatTime(
                                            schedule.startTime,
                                            locale,
                                            use24HourFormat
                                        )
                                    } → ${
                                        formatTime(
                                            schedule.endTime,
                                            locale,
                                            use24HourFormat
                                        )
                                    }",
                                style =
                                    MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(">")
                    }
                }
            }

            Button(
                onClick = {
                    onEditSchedule(
                        null,
                        null
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.add_schedule
                    )
                )
            }

            Text(
                text =
                    stringResource(
                        R.string.do_not_disturb
                    ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onEditDndPolicy()
                    }
                    .padding(
                        vertical = 12.dp
                    )
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(
                            R.string.dnd_settings
                        )
                    )

                    Text(
                        text =
                            dndSummary(
                                dndPolicy
                            ),
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

                Text(">")
            }

            Button(
                onClick = {
                    localErrorMessage = null
                    internalConflict = null

                    if (schedules.isEmpty()) {
                        localErrorMessage =
                            addAtLeastOneScheduleMessage

                        return@Button
                    }

                    for (
                    firstIndex in schedules.indices
                    ) {
                        for (
                        secondIndex in
                        firstIndex + 1 until schedules.size
                        ) {
                            val conflict =
                                findConflict(
                                    schedules[firstIndex],
                                    schedules[secondIndex]
                                )

                            if (conflict != null) {
                                internalConflict =
                                    InternalScheduleConflict(
                                        firstIndex =
                                            firstIndex,
                                        secondIndex =
                                            secondIndex,
                                        conflict =
                                            conflict
                                    )

                                return@Button
                            }
                        }
                    }

                    onSave(
                        name.trim(),
                        schedules,
                        dndPolicy
                    )
                },
                enabled =
                    name.isNotBlank() &&
                            schedules.isNotEmpty(),
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (isEditing) {
                            R.string.save_changes
                        } else {
                            R.string.save_profile
                        }
                    )
                )
            }

            if (
                isEditing &&
                onDuplicateProfile != null
            ) {
                Button(
                    onClick =
                        onDuplicateProfile,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            R.string.duplicate_profile
                        )
                    )
                }
            }

            if (
                isEditing &&
                onDeleteProfile != null
            ) {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string.delete_profile
                            ),
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun dndSummary(
    policy: DndPolicy
): String {
    val enabledCount =
        listOf(
            policy.allowAlarms,
            policy.allowReminders,
            policy.allowEvents,
            policy.allowMedia,
            policy.allowSystem,
            policy.allowRepeatCallers,
            policy.calls.enabled,
            policy.messages.enabled,
            policy.conversations.enabled
        ).count { it }

    return if (enabledCount == 0) {
        stringResource(
            R.string.no_priority_interruptions
        )
    } else {
        stringResource(
            R.string.priority_options_enabled,
            enabledCount
        )
    }
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