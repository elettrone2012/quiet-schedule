package io.github.elettrone2012.quietschedule.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elettrone2012.quietschedule.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    showStatusNotification: Boolean,
    notificationPermissionGranted: Boolean,
    onShowStatusNotificationChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.settings
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
                .padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.notifications
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            end = 16.dp
                        )
                ) {
                    Text(
                        text = stringResource(
                            R.string.show_status_notification
                        )
                    )

                    Text(
                        text = stringResource(
                            R.string.show_status_notification_description
                        ),
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

                Switch(
                    checked =
                        showStatusNotification,
                    onCheckedChange =
                        onShowStatusNotificationChange
                )
            }

            if (
                showStatusNotification &&
                !notificationPermissionGranted
            ) {
                Text(
                    text = stringResource(
                        R.string.notification_permission_required
                    ),
                    color =
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}