package io.github.elettrone2012.quietschedule.ui.profile

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elettrone2012.quietschedule.R
import io.github.elettrone2012.quietschedule.domain.model.ConversationCategory
import io.github.elettrone2012.quietschedule.domain.model.ConversationScope
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.SenderCategory
import io.github.elettrone2012.quietschedule.domain.model.SenderScope
import io.github.elettrone2012.quietschedule.domain.model.SuppressedVisualEffects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndPolicyEditorScreen(
    policy: DndPolicy,
    onBack: () -> Unit,
    onSave: (DndPolicy) -> Unit
) {
    var allowAlarms by remember(policy) {
        mutableStateOf(policy.allowAlarms)
    }

    var allowReminders by remember(policy) {
        mutableStateOf(policy.allowReminders)
    }

    var allowEvents by remember(policy) {
        mutableStateOf(policy.allowEvents)
    }

    var allowMedia by remember(policy) {
        mutableStateOf(policy.allowMedia)
    }

    var allowSystem by remember(policy) {
        mutableStateOf(policy.allowSystem)
    }

    var allowRepeatCallers by remember(policy) {
        mutableStateOf(policy.allowRepeatCallers)
    }

    var callsEnabled by remember(policy) {
        mutableStateOf(policy.calls.enabled)
    }

    var callsSender by remember(policy) {
        mutableStateOf(policy.calls.sender)
    }

    var messagesEnabled by remember(policy) {
        mutableStateOf(policy.messages.enabled)
    }

    var messagesSender by remember(policy) {
        mutableStateOf(policy.messages.sender)
    }

    var conversationsEnabled by remember(policy) {
        mutableStateOf(policy.conversations.enabled)
    }

    var conversationsSender by remember(policy) {
        mutableStateOf(policy.conversations.sender)
    }

    var suppressFullScreenIntent by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.fullScreenIntent
        )
    }

    var suppressLights by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.lights
        )
    }

    var suppressPeek by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.peek
        )
    }

    var suppressStatusBar by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.statusBar
        )
    }

    var suppressBadge by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.badge
        )
    }

    var suppressAmbient by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.ambient
        )
    }

    var suppressNotificationList by remember(policy) {
        mutableStateOf(
            policy.suppressedVisualEffects.notificationList
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.dnd_settings
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
                    R.string.priority_interruptions
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            PolicySwitch(
                label = stringResource(
                    R.string.alarms
                ),
                checked = allowAlarms,
                onCheckedChange = {
                    allowAlarms = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.reminders
                ),
                checked = allowReminders,
                onCheckedChange = {
                    allowReminders = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.events
                ),
                checked = allowEvents,
                onCheckedChange = {
                    allowEvents = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.media
                ),
                checked = allowMedia,
                onCheckedChange = {
                    allowMedia = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.system
                ),
                checked = allowSystem,
                onCheckedChange = {
                    allowSystem = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.repeat_callers
                ),
                checked = allowRepeatCallers,
                onCheckedChange = {
                    allowRepeatCallers = it
                }
            )

            Text(
                text = stringResource(
                    R.string.calls
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            PolicySwitch(
                label = stringResource(
                    R.string.allow_calls
                ),
                checked = callsEnabled,
                onCheckedChange = {
                    callsEnabled = it
                }
            )

            SenderScopeSelector(
                enabled = callsEnabled,
                selected = callsSender,
                onSelected = {
                    callsSender = it
                }
            )

            Text(
                text = stringResource(
                    R.string.messages
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            PolicySwitch(
                label = stringResource(
                    R.string.allow_messages
                ),
                checked = messagesEnabled,
                onCheckedChange = {
                    messagesEnabled = it
                }
            )

            SenderScopeSelector(
                enabled = messagesEnabled,
                selected = messagesSender,
                onSelected = {
                    messagesSender = it
                }
            )

            Text(
                text = stringResource(
                    R.string.conversations
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            PolicySwitch(
                label = stringResource(
                    R.string.allow_conversations
                ),
                checked = conversationsEnabled,
                onCheckedChange = {
                    conversationsEnabled = it
                }
            )

            ConversationScopeSelector(
                enabled = conversationsEnabled,
                selected = conversationsSender,
                onSelected = {
                    conversationsSender = it
                }
            )

            Text(
                text = stringResource(
                    R.string.suppress_visual_effects
                ),
                style =
                    MaterialTheme.typography.titleMedium
            )

            PolicySwitch(
                label = stringResource(
                    R.string.full_screen_intent
                ),
                checked = suppressFullScreenIntent,
                onCheckedChange = {
                    suppressFullScreenIntent = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.lights
                ),
                checked = suppressLights,
                onCheckedChange = {
                    suppressLights = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.peek
                ),
                checked = suppressPeek,
                onCheckedChange = {
                    suppressPeek = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.status_bar
                ),
                checked = suppressStatusBar,
                onCheckedChange = {
                    suppressStatusBar = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.badge
                ),
                checked = suppressBadge,
                onCheckedChange = {
                    suppressBadge = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.ambient
                ),
                checked = suppressAmbient,
                onCheckedChange = {
                    suppressAmbient = it
                }
            )

            PolicySwitch(
                label = stringResource(
                    R.string.notification_list
                ),
                checked = suppressNotificationList,
                onCheckedChange = {
                    suppressNotificationList = it
                }
            )

            Button(
                onClick = {
                    onSave(
                        DndPolicy(
                            allowAlarms = allowAlarms,
                            allowReminders = allowReminders,
                            allowEvents = allowEvents,
                            allowMedia = allowMedia,
                            allowSystem = allowSystem,
                            allowRepeatCallers =
                                allowRepeatCallers,
                            calls = SenderCategory(
                                enabled = callsEnabled,
                                sender = callsSender
                            ),
                            messages = SenderCategory(
                                enabled = messagesEnabled,
                                sender = messagesSender
                            ),
                            conversations =
                                ConversationCategory(
                                    enabled =
                                        conversationsEnabled,
                                    sender =
                                        conversationsSender
                                ),
                            suppressedVisualEffects =
                                SuppressedVisualEffects(
                                    fullScreenIntent =
                                        suppressFullScreenIntent,
                                    lights =
                                        suppressLights,
                                    peek =
                                        suppressPeek,
                                    statusBar =
                                        suppressStatusBar,
                                    badge =
                                        suppressBadge,
                                    ambient =
                                        suppressAmbient,
                                    notificationList =
                                        suppressNotificationList
                                )
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        R.string.save_dnd_settings
                    )
                )
            }
        }
    }
}

@Composable
private fun PolicySwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(
                    end = 16.dp,
                    top = 12.dp
                )
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SenderScopeSelector(
    enabled: Boolean,
    selected: SenderScope,
    onSelected: (SenderScope) -> Unit
) {
    Column {
        SenderScope.entries.forEach { scope ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected =
                        selected == scope,
                    onClick = {
                        if (enabled) {
                            onSelected(scope)
                        }
                    },
                    enabled = enabled
                )

                Text(
                    text =
                        when (scope) {
                            SenderScope.ANYONE ->
                                stringResource(
                                    R.string.anyone
                                )

                            SenderScope.CONTACTS ->
                                stringResource(
                                    R.string.contacts
                                )

                            SenderScope.STARRED ->
                                stringResource(
                                    R.string.starred
                                )
                        },
                    modifier =
                        Modifier.padding(
                            top = 12.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun ConversationScopeSelector(
    enabled: Boolean,
    selected: ConversationScope,
    onSelected: (ConversationScope) -> Unit
) {
    Column {
        ConversationScope.entries.forEach { scope ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected =
                        selected == scope,
                    onClick = {
                        if (enabled) {
                            onSelected(scope)
                        }
                    },
                    enabled = enabled
                )

                Text(
                    text =
                        when (scope) {
                            ConversationScope.ANYONE ->
                                stringResource(
                                    R.string.anyone
                                )

                            ConversationScope.IMPORTANT ->
                                stringResource(
                                    R.string.important
                                )
                        },
                    modifier =
                        Modifier.padding(
                            top = 12.dp
                        )
                )
            }
        }
    }
}