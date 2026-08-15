package io.github.elettrone2012.quietschedule

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.github.elettrone2012.quietschedule.app.QuietScheduleFactory
import io.github.elettrone2012.quietschedule.data.datastore.QuietScheduleRepository
import io.github.elettrone2012.quietschedule.domain.model.AppSettings
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import io.github.elettrone2012.quietschedule.domain.profile.EnableProfileResult
import io.github.elettrone2012.quietschedule.domain.profile.SaveProfileResult
import io.github.elettrone2012.quietschedule.domain.profile.deleteProfile
import io.github.elettrone2012.quietschedule.domain.profile.disableProfile
import io.github.elettrone2012.quietschedule.domain.profile.duplicateProfile
import io.github.elettrone2012.quietschedule.domain.profile.enableProfile
import io.github.elettrone2012.quietschedule.domain.profile.saveProfile
import io.github.elettrone2012.quietschedule.platform.dnd.DndController
import io.github.elettrone2012.quietschedule.platform.notifications.StatusNotificationManager
import io.github.elettrone2012.quietschedule.ui.home.HomeScreen
import io.github.elettrone2012.quietschedule.ui.profile.DndPolicyEditorScreen
import io.github.elettrone2012.quietschedule.ui.profile.ProfileEditorScreen
import io.github.elettrone2012.quietschedule.ui.profile.ScheduleEditorScreen
import io.github.elettrone2012.quietschedule.ui.settings.SettingsScreen
import io.github.elettrone2012.quietschedule.ui.theme.QuietScheduleTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import io.github.elettrone2012.quietschedule.ui.help.HelpScreen

class MainActivity : ComponentActivity() {

    private var dndAccessGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        refreshDndAccess()

        setContent {
            QuietScheduleTheme {
                val repository =
                    QuietScheduleRepository(
                        applicationContext
                    )

                val profiles by
                repository.profiles.collectAsState(
                    initial = emptyList()
                )

                val appSettings by
                repository.appSettings.collectAsState(
                    initial = AppSettings()
                )

                var errorMessage by remember {
                    mutableStateOf<String?>(null)
                }

                var showProfileEditor by remember {
                    mutableStateOf(false)
                }

                var showScheduleEditor by remember {
                    mutableStateOf(false)
                }

                var showDndPolicyEditor by remember {
                    mutableStateOf(false)
                }

                var showSettings by remember {
                    mutableStateOf(false)
                }

                var showHelp by remember {
                    mutableStateOf(false)
                }

                var editingProfileId by remember {
                    mutableStateOf<String?>(null)
                }

                /*
                 * Stato temporaneo modificabile
                 * del profilo aperto nell'editor.
                 */
                var profileName by remember {
                    mutableStateOf("")
                }

                var profileDndPolicy by remember {
                    mutableStateOf(
                        DndPolicy()
                    )
                }

                val profileSchedules =
                    remember {
                        mutableStateListOf<Schedule>()
                    }

                /*
                 * Snapshot immutabile dello stato originale.
                 *
                 * Serve per rilevare modifiche non salvate,
                 * anche quando si passa attraverso gli editor
                 * delle fasce o delle impostazioni DND.
                 */
                var originalProfileName by remember {
                    mutableStateOf("")
                }

                var originalProfileSchedules by remember {
                    mutableStateOf<List<Schedule>>(
                        emptyList()
                    )
                }

                var originalProfileDndPolicy by remember {
                    mutableStateOf(
                        DndPolicy()
                    )
                }

                var editingScheduleIndex by remember {
                    mutableStateOf<Int?>(null)
                }

                var notificationPermissionGranted
                        by remember {
                            mutableStateOf(
                                hasNotificationPermission()
                            )
                        }

                val statusNotificationManager =
                    remember {
                        StatusNotificationManager(
                            applicationContext
                        )
                    }

                val notificationPermissionLauncher =
                    rememberLauncherForActivityResult(
                        contract =
                            ActivityResultContracts
                                .RequestPermission()
                    ) { granted ->
                        notificationPermissionGranted =
                            granted

                        lifecycleScope.launch {
                            repository
                                .setShowStatusNotification(
                                    granted
                                )

                            if (!granted) {
                                statusNotificationManager
                                    .cancel()
                            }
                        }
                    }

                val editingSchedule =
                    editingScheduleIndex?.let { index ->
                        profileSchedules
                            .getOrNull(index)
                    }

                fun closeProfileEditor() {
                    errorMessage = null

                    editingProfileId = null

                    profileName = ""
                    profileDndPolicy =
                        DndPolicy()
                    profileSchedules.clear()

                    originalProfileName = ""
                    originalProfileSchedules =
                        emptyList()
                    originalProfileDndPolicy =
                        DndPolicy()

                    editingScheduleIndex = null

                    showScheduleEditor = false
                    showDndPolicyEditor = false
                    showProfileEditor = false
                }

                fun profileConflictMessage(
                    profileName: String,
                    day: java.time.DayOfWeek,
                    start: LocalTime,
                    end: LocalTime
                ): String {
                    val locales =
                        resources.configuration.locales

                    val locale =
                        if (locales.isEmpty) {
                            Locale.getDefault()
                        } else {
                            locales[0]
                        }

                    val use24HourFormat =
                        DateFormat.is24HourFormat(
                            this@MainActivity
                        )

                    val timeFormatter =
                        DateTimeFormatter.ofPattern(
                            if (use24HourFormat) {
                                "HH:mm"
                            } else {
                                "h:mm a"
                            },
                            locale
                        )

                    val localizedDay =
                        day.getDisplayName(
                            TextStyle.FULL,
                            locale
                        )

                    return getString(
                        R.string.profile_conflict,
                        profileName,
                        localizedDay,
                        start.format(
                            timeFormatter
                        ),
                        end.format(
                            timeFormatter
                        )
                    )
                }

                when {
                    showScheduleEditor -> {
                        ScheduleEditorScreen(
                            schedule =
                                editingSchedule,
                            onBack = {
                                showScheduleEditor =
                                    false

                                editingScheduleIndex =
                                    null
                            },
                            onSave = { schedule ->
                                val index =
                                    editingScheduleIndex

                                if (index == null) {
                                    profileSchedules.add(
                                        schedule
                                    )
                                } else {
                                    profileSchedules[index] =
                                        schedule
                                }

                                showScheduleEditor =
                                    false

                                editingScheduleIndex =
                                    null
                            },
                            onDelete =
                                if (
                                    editingScheduleIndex !=
                                    null
                                ) {
                                    {
                                        val index =
                                            editingScheduleIndex

                                        if (
                                            index != null &&
                                            index in
                                            profileSchedules.indices
                                        ) {
                                            profileSchedules
                                                .removeAt(
                                                    index
                                                )
                                        }

                                        showScheduleEditor =
                                            false

                                        editingScheduleIndex =
                                            null
                                    }
                                } else {
                                    null
                                }
                        )
                    }

                    showDndPolicyEditor -> {
                        DndPolicyEditorScreen(
                            policy =
                                profileDndPolicy,
                            onBack = {
                                showDndPolicyEditor =
                                    false
                            },
                            onSave = { policy ->
                                profileDndPolicy =
                                    policy

                                showDndPolicyEditor =
                                    false
                            }
                        )
                    }

                    showProfileEditor -> {
                        ProfileEditorScreen(
                            isEditing =
                                editingProfileId != null,

                            name =
                                profileName,

                            onNameChange = {
                                profileName = it
                            },

                            schedules =
                                profileSchedules,

                            dndPolicy =
                                profileDndPolicy,

                            originalName =
                                originalProfileName,

                            originalSchedules =
                                originalProfileSchedules,

                            originalDndPolicy =
                                originalProfileDndPolicy,

                            errorMessage =
                                errorMessage,

                            onBack = {
                                closeProfileEditor()
                            },

                            onEditSchedule = { _, index ->
                                editingScheduleIndex =
                                    index

                                showScheduleEditor =
                                    true
                            },

                            onEditDndPolicy = {
                                showDndPolicyEditor =
                                    true
                            },

                            onSave = {
                                    name,
                                    schedules,
                                    dndPolicy ->

                                lifecycleScope.launch {
                                    val profileId =
                                        editingProfileId

                                    if (profileId == null) {
                                        val newProfile =
                                            Profile(
                                                name =
                                                    name,
                                                enabled =
                                                    false,
                                                dndPolicy =
                                                    dndPolicy,
                                                schedules =
                                                    schedules
                                            )

                                        repository
                                            .saveProfiles(
                                                profiles +
                                                        newProfile
                                            )
                                    } else {
                                        val originalProfile =
                                            profiles
                                                .firstOrNull {
                                                    it.id ==
                                                            profileId
                                                }

                                        if (
                                            originalProfile ==
                                            null
                                        ) {
                                            errorMessage =
                                                getString(
                                                    R.string
                                                        .profile_not_found
                                                )

                                            return@launch
                                        }

                                        val updatedProfile =
                                            originalProfile
                                                .copy(
                                                    name =
                                                        name,
                                                    schedules =
                                                        schedules,
                                                    dndPolicy =
                                                        dndPolicy
                                                )

                                        when (
                                            val result =
                                                saveProfile(
                                                    updatedProfile =
                                                        updatedProfile,
                                                    profiles =
                                                        profiles
                                                )
                                        ) {
                                            is SaveProfileResult.Success -> {
                                                repository
                                                    .saveProfiles(
                                                        result.profiles
                                                    )
                                            }

                                            is SaveProfileResult.Conflict -> {
                                                val conflict =
                                                    result.details
                                                        .scheduleConflict

                                                errorMessage =
                                                    profileConflictMessage(
                                                        result.details
                                                            .conflictingProfile
                                                            .name,
                                                        conflict.dayOfWeek,
                                                        conflict.overlapStart,
                                                        conflict.overlapEnd
                                                    )

                                                return@launch
                                            }

                                            SaveProfileResult.ProfileNotFound -> {
                                                errorMessage =
                                                    getString(
                                                        R.string
                                                            .profile_not_found
                                                    )

                                                return@launch
                                            }
                                        }
                                    }

                                    closeProfileEditor()

                                    QuietScheduleFactory
                                        .createCoordinator(
                                            applicationContext
                                        )
                                        .reconcile(
                                            LocalDateTime.now()
                                        )
                                }
                            },

                            onDuplicateProfile =
                                if (
                                    editingProfileId != null
                                ) {
                                    {
                                        val sourceId =
                                            editingProfileId

                                        if (
                                            sourceId != null
                                        ) {
                                            val newId =
                                                UUID
                                                    .randomUUID()
                                                    .toString()

                                            val duplicated =
                                                duplicateProfile(
                                                    profileId =
                                                        sourceId,
                                                    profiles =
                                                        profiles,
                                                    newProfileId =
                                                        newId
                                                )

                                            val copy =
                                                duplicated
                                                    .firstOrNull {
                                                        it.id ==
                                                                newId
                                                    }

                                            if (
                                                copy != null
                                            ) {
                                                lifecycleScope
                                                    .launch {
                                                        repository
                                                            .saveProfiles(
                                                                duplicated
                                                            )

                                                        editingProfileId =
                                                            copy.id

                                                        profileName =
                                                            copy.name

                                                        profileDndPolicy =
                                                            copy.dndPolicy

                                                        profileSchedules
                                                            .clear()

                                                        profileSchedules
                                                            .addAll(
                                                                copy.schedules
                                                            )

                                                        /*
                                                         * La copia è già stata
                                                         * persistita: diventa
                                                         * quindi la nuova
                                                         * baseline.
                                                         */
                                                        originalProfileName =
                                                            copy.name

                                                        originalProfileDndPolicy =
                                                            copy.dndPolicy

                                                        originalProfileSchedules =
                                                            copy.schedules
                                                                .toList()

                                                        editingScheduleIndex =
                                                            null

                                                        errorMessage =
                                                            null
                                                    }
                                            }
                                        }
                                    }
                                } else {
                                    null
                                },

                            onDeleteProfile =
                                if (
                                    editingProfileId != null
                                ) {
                                    {
                                        lifecycleScope.launch {
                                            val profileId =
                                                editingProfileId
                                                    ?: return@launch

                                            val updatedProfiles =
                                                deleteProfile(
                                                    profileId =
                                                        profileId,
                                                    profiles =
                                                        profiles
                                                )

                                            repository
                                                .saveProfiles(
                                                    updatedProfiles
                                                )

                                            closeProfileEditor()

                                            QuietScheduleFactory
                                                .createCoordinator(
                                                    applicationContext
                                                )
                                                .reconcile(
                                                    LocalDateTime.now()
                                                )
                                        }
                                    }
                                } else {
                                    null
                                }
                        )
                    }

                    showSettings -> {
                        SettingsScreen(
                            showStatusNotification =
                                appSettings
                                    .showStatusNotification,

                            notificationPermissionGranted =
                                notificationPermissionGranted,

                            onShowStatusNotificationChange = {
                                    enabled ->

                                if (!enabled) {
                                    lifecycleScope.launch {
                                        repository
                                            .setShowStatusNotification(
                                                false
                                            )

                                        statusNotificationManager
                                            .cancel()
                                    }
                                } else {
                                    if (
                                        Build.VERSION.SDK_INT >=
                                        Build.VERSION_CODES
                                            .TIRAMISU &&
                                        !hasNotificationPermission()
                                    ) {
                                        notificationPermissionLauncher
                                            .launch(
                                                Manifest.permission
                                                    .POST_NOTIFICATIONS
                                            )
                                    } else {
                                        notificationPermissionGranted =
                                            true

                                        lifecycleScope.launch {
                                            repository
                                                .setShowStatusNotification(
                                                    true
                                                )
                                        }
                                    }
                                }
                            },

                            onBack = {
                                showSettings = false
                            }
                        )
                    }

                    showHelp -> {
                        HelpScreen(
                            onBack = {
                                showHelp = false
                            }
                        )
                    }

                    else -> {
                        HomeScreen(
                            profiles =
                                profiles,

                            errorMessage =
                                errorMessage,

                            dndAccessGranted =
                                dndAccessGranted,

                            onRequestDndAccess = {
                                startActivity(
                                    Intent(
                                        Settings
                                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                                    )
                                )
                            },

                            onNewProfile = {
                                errorMessage = null

                                editingProfileId =
                                    null

                                profileName = ""

                                profileDndPolicy =
                                    DndPolicy()

                                profileSchedules.clear()

                                /*
                                 * Baseline vuota per un
                                 * nuovo profilo.
                                 */
                                originalProfileName = ""

                                originalProfileDndPolicy =
                                    DndPolicy()

                                originalProfileSchedules =
                                    emptyList()

                                editingScheduleIndex =
                                    null

                                showProfileEditor =
                                    true
                            },

                            onProfileClick = { profile ->
                                errorMessage = null

                                editingProfileId =
                                    profile.id

                                /*
                                 * Copia modificabile.
                                 */
                                profileName =
                                    profile.name

                                profileDndPolicy =
                                    profile.dndPolicy

                                profileSchedules.clear()

                                profileSchedules.addAll(
                                    profile.schedules
                                )

                                /*
                                 * Snapshot originale.
                                 *
                                 * Non deve essere modificato
                                 * durante l'editing.
                                 */
                                originalProfileName =
                                    profile.name

                                originalProfileDndPolicy =
                                    profile.dndPolicy

                                originalProfileSchedules =
                                    profile.schedules
                                        .toList()

                                editingScheduleIndex =
                                    null

                                showProfileEditor =
                                    true
                            },

                            onProfileEnabledChange = {
                                    profile,
                                    enabled ->

                                lifecycleScope.launch {
                                    if (
                                        enabled &&
                                        !dndAccessGranted
                                    ) {
                                        errorMessage =
                                            getString(
                                                R.string
                                                    .dnd_access_required_before_enable
                                            )

                                        return@launch
                                    }

                                    val updatedProfiles =
                                        if (enabled) {
                                            when (
                                                val result =
                                                    enableProfile(
                                                        profileId =
                                                            profile.id,
                                                        profiles =
                                                            profiles
                                                    )
                                            ) {
                                                is EnableProfileResult.Success -> {
                                                    errorMessage =
                                                        null

                                                    result.profiles
                                                }

                                                is EnableProfileResult.Conflict -> {
                                                    val conflict =
                                                        result.details
                                                            .scheduleConflict

                                                    errorMessage =
                                                        profileConflictMessage(
                                                            result.details
                                                                .conflictingProfile
                                                                .name,
                                                            conflict.dayOfWeek,
                                                            conflict.overlapStart,
                                                            conflict.overlapEnd
                                                        )

                                                    return@launch
                                                }

                                                EnableProfileResult.ProfileNotFound -> {
                                                    errorMessage =
                                                        getString(
                                                            R.string
                                                                .profile_not_found
                                                        )

                                                    return@launch
                                                }
                                            }
                                        } else {
                                            errorMessage = null

                                            disableProfile(
                                                profileId =
                                                    profile.id,
                                                profiles =
                                                    profiles
                                            )
                                        }

                                    repository
                                        .saveProfiles(
                                            updatedProfiles
                                        )

                                    QuietScheduleFactory
                                        .createCoordinator(
                                            applicationContext
                                        )
                                        .reconcile(
                                            LocalDateTime.now()
                                        )
                                }
                            },

                            onSettings = {
                                notificationPermissionGranted =
                                    hasNotificationPermission()

                                showSettings = true
                            },
                            onHelp = {
                                showHelp = true
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        refreshDndAccess()

        lifecycleScope.launch {
            QuietScheduleFactory
                .createCoordinator(
                    applicationContext
                )
                .reconcile(
                    LocalDateTime.now()
                )
        }
    }

    private fun refreshDndAccess() {
        dndAccessGranted =
            DndController(
                applicationContext
            ).hasPolicyAccess()
    }

    private fun hasNotificationPermission(): Boolean {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}