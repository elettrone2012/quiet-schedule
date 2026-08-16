package io.github.elettrone2012.quietschedule.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.elettrone2012.quietschedule.R
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private enum class EditingTime {
    START,
    END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorScreen(
    schedule: Schedule? = null,
    onBack: () -> Unit,
    onSave: (Schedule) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var selectedDays by remember(schedule) {
        mutableStateOf(
            schedule?.daysOfWeek
                ?: emptySet()
        )
    }

    var startMinute by remember(schedule) {
        mutableStateOf(
            schedule?.startMinute
                ?: 9 * 60
        )
    }

    var endMinute by remember(schedule) {
        mutableStateOf(
            schedule?.endMinute
                ?: 17 * 60
        )
    }

    var editingTime by remember {
        mutableStateOf<EditingTime?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val configuration =
        LocalConfiguration.current

    val locale =
        if (configuration.locales.isEmpty) {
            Locale.getDefault()
        } else {
            configuration.locales[0]
        }

    val timeFormatter =
        remember(locale) {
            DateTimeFormatter
                .ofLocalizedTime(
                    FormatStyle.SHORT
                )
                .withLocale(locale)
        }

    val selectDayError =
        stringResource(
            R.string.select_at_least_one_day
        )

    val endTimeError =
        stringResource(
            R.string.end_time_after_start
        )

    editingTime?.let { target ->
        val initialMinute =
            when (target) {
                EditingTime.START ->
                    startMinute

                EditingTime.END ->
                    endMinute
            }

        val initialTime =
            Schedule.minuteToLocalTime(
                initialMinute
            )

        val timePickerState =
            rememberTimePickerState(
                initialHour =
                    initialTime.hour,
                initialMinute =
                    initialTime.minute
            )

        TimePickerDialog(
            onDismissRequest = {
                editingTime = null
            },
            title = {
                Text(
                    stringResource(
                        if (
                            target ==
                            EditingTime.START
                        ) {
                            R.string.select_start_time
                        } else {
                            R.string.select_end_time
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMinute =
                            timePickerState.hour * 60 +
                                    timePickerState.minute

                        when (target) {
                            EditingTime.START -> {
                                startMinute =
                                    selectedMinute
                            }

                            EditingTime.END -> {
                                endMinute =
                                    if (selectedMinute == 0) {
                                        Schedule.MINUTES_PER_DAY
                                    } else {
                                        selectedMinute
                                    }
                            }
                        }

                        errorMessage = null
                        editingTime = null
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.ok
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingTime = null
                    }
                ) {
                    Text(
                        stringResource(
                            R.string.cancel
                        )
                    )
                }
            }
        ) {
            TimePicker(
                state = timePickerState
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (
                                schedule == null
                            ) {
                                R.string.new_schedule
                            } else {
                                R.string.edit_schedule
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
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
            Text(
                text = stringResource(
                    R.string.days
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            FlowRow(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                DayOfWeek.entries.forEach { day ->

                    val selected =
                        day in selectedDays

                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedDays =
                                if (selected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }

                            errorMessage = null
                        },
                        label = {
                            Text(
                                text =
                                    day.getDisplayName(
                                        TextStyle.SHORT,
                                        locale
                                    )
                            )
                        }
                    )
                }
            }

            Text(
                text = stringResource(
                    R.string.time
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            TimeRow(
                label =
                    stringResource(
                        R.string.start_time
                    ),
                value =
                    Schedule
                        .minuteToLocalTime(
                            startMinute
                        )
                        .format(
                            timeFormatter
                        ),
                onClick = {
                    editingTime =
                        EditingTime.START
                }
            )

            TimeRow(
                label =
                    stringResource(
                        R.string.end_time
                    ),
                value =
                    Schedule
                        .minuteToLocalTime(
                            endMinute
                        )
                        .format(
                            timeFormatter
                        ),
                onClick = {
                    editingTime =
                        EditingTime.END
                }
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color =
                        MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (
                        selectedDays.isEmpty()
                    ) {
                        errorMessage =
                            selectDayError

                        return@Button
                    }

                    if (
                        endMinute <= startMinute ||
                        (
                                startMinute == 0 &&
                                        endMinute ==
                                        Schedule.MINUTES_PER_DAY
                                )
                    ) {
                        errorMessage =
                            endTimeError

                        return@Button
                    }

                    val parsedSchedule =
                        Schedule(
                            daysOfWeek =
                                selectedDays,
                            startMinute =
                                startMinute,
                            endMinute =
                                endMinute
                        )

                    errorMessage = null

                    onSave(
                        parsedSchedule
                    )
                },
                enabled =
                    selectedDays.isNotEmpty(),
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.save_schedule
                    )
                )
            }

            if (
                onDelete != null
            ) {
                TextButton(
                    onClick =
                        onDelete,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(
                            R.string.delete_schedule
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                vertical = 12.dp
            ),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text = label
        )

        Text(
            text = value,
            fontWeight =
                FontWeight.Medium
        )
    }
}