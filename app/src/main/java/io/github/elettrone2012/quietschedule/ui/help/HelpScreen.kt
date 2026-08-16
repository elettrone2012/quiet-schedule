package io.github.elettrone2012.quietschedule.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elettrone2012.quietschedule.R

private data class HelpSection(
    val titleRes: Int,
    val bodyRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    val sections = listOf(
        HelpSection(R.string.help_what_title, R.string.help_what_body),
        HelpSection(R.string.help_first_launch_title, R.string.help_first_launch_body),
        HelpSection(R.string.help_profiles_title, R.string.help_profiles_body),
        HelpSection(R.string.help_schedules_title, R.string.help_schedules_body),
        HelpSection(R.string.help_conflicts_title, R.string.help_conflicts_body),
        HelpSection(R.string.help_dnd_title, R.string.help_dnd_body),
        HelpSection(R.string.help_calls_messages_title, R.string.help_calls_messages_body),
        HelpSection(R.string.help_apps_title, R.string.help_apps_body),
        HelpSection(R.string.help_visual_effects_title, R.string.help_visual_effects_body),
        HelpSection(R.string.help_examples_title, R.string.help_examples_body),
        HelpSection(R.string.help_manual_dnd_title, R.string.help_manual_dnd_body),
        HelpSection(R.string.help_time_changes_title, R.string.help_time_changes_body),
        HelpSection(R.string.help_precision_title, R.string.help_precision_body),
        HelpSection(R.string.help_status_notification_title, R.string.help_status_notification_body),
        HelpSection(R.string.help_privacy_title, R.string.help_privacy_body),
        HelpSection(R.string.help_limits_title, R.string.help_limits_body),
        HelpSection(
            R.string.help_compatibility_title,
            R.string.help_compatibility_body
        ),HelpSection(R.string.help_development_title,R.string.help_development_body)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.help_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            sections.forEach { section ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = stringResource(section.bodyRes),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}