package io.github.elettrone2012.quietschedule.app

import io.github.elettrone2012.quietschedule.data.datastore.QuietScheduleRepositoryContract
import io.github.elettrone2012.quietschedule.domain.model.AppSettings
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy
import io.github.elettrone2012.quietschedule.domain.model.Profile
import io.github.elettrone2012.quietschedule.domain.model.Schedule
import io.github.elettrone2012.quietschedule.domain.scheduling.ScheduleEvent
import io.github.elettrone2012.quietschedule.platform.dnd.DndGateway
import io.github.elettrone2012.quietschedule.platform.notifications.StatusNotificationGateway
import io.github.elettrone2012.quietschedule.platform.scheduling.SchedulingGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class QuietScheduleCoordinatorTest {

    @Test
    fun missingPolicyAccessDisablesAllProfilesAndPersistsThem() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = true,
                    schedules = emptyList()
                )
            )
        )

        val dnd = FakeDndGateway(
            policyAccess = false
        )

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = FakeSchedulingGateway()
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertTrue(
            repository.savedProfiles.all { !it.enabled }
        )
        assertFalse(dnd.priorityModeApplied)
        assertFalse(dnd.dndDisabled)
    }

    @Test
    fun activeProfileAppliesPriorityMode() = runTest {
        val policy = DndPolicy(
            allowAlarms = true
        )

        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = true,
                    dndPolicy = policy,
                    schedules = listOf(
                        Schedule(
                            daysOfWeek = setOf(DayOfWeek.MONDAY),
                            startMinute = 9 * 60,
                            endMinute = 17 * 60
                        )
                    )
                )
            )
        )

        val dnd = FakeDndGateway(
            policyAccess = true
        )

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = FakeSchedulingGateway()
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertTrue(dnd.priorityModeApplied)
        assertEquals(policy, dnd.appliedPolicy)
        assertFalse(dnd.dndDisabled)
    }

    @Test
    fun noActiveProfileDisablesDnd() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = true,
                    schedules = listOf(
                        Schedule(
                            daysOfWeek = setOf(DayOfWeek.MONDAY),
                            startMinute = 9 * 60,
                            endMinute = 17 * 60
                        )
                    )
                )
            )
        )

        val dnd = FakeDndGateway(
            policyAccess = true
        )

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = FakeSchedulingGateway()
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 18, 0)
        )

        assertTrue(dnd.dndDisabled)
        assertFalse(dnd.priorityModeApplied)
    }

    @Test
    fun reconcileSchedulesNextEvent() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = true,
                    schedules = listOf(
                        Schedule(
                            daysOfWeek = setOf(DayOfWeek.MONDAY),
                            startMinute = 9 * 60,
                            endMinute = 17 * 60
                        )
                    )
                )
            )
        )

        val dnd = FakeDndGateway(
            policyAccess = true
        )

        val scheduling = FakeSchedulingGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = scheduling
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 8, 0)
        )

        assertEquals(
            LocalDateTime.of(2026, 8, 10, 9, 0),
            scheduling.scheduledEvent?.dateTime
        )
    }

    @Test
    fun midnightEndScheduleRemainsActiveAndEndsNextDayAtMidnight() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "evening",
                    name = "Evening",
                    enabled = true,
                    schedules = listOf(
                        Schedule(
                            daysOfWeek = setOf(DayOfWeek.MONDAY),
                            startMinute = 22 * 60,
                            endMinute = Schedule.MINUTES_PER_DAY
                        )
                    )
                )
            )
        )

        val dnd = FakeDndGateway(
            policyAccess = true
        )

        val scheduling = FakeSchedulingGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = scheduling
        )

        coordinator.reconcile(
            LocalDateTime.of(
                2026,
                8,
                10,
                23,
                30
            )
        )

        assertTrue(dnd.priorityModeApplied)
        assertFalse(dnd.dndDisabled)

        assertEquals(
            LocalDateTime.of(
                2026,
                8,
                11,
                0,
                0
            ),
            scheduling.scheduledEvent?.dateTime
        )
    }

    @Test
    fun missingPolicyAccessCancelsScheduling() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = true,
                    schedules = emptyList()
                )
            )
        )

        val dnd = FakeDndGateway(
            policyAccess = false
        )

        val scheduling = FakeSchedulingGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = scheduling
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertTrue(scheduling.cancelAllCalled)
    }

    @Test
    fun noFutureEventsSchedulesNull() = runTest {
        val repository = FakeRepository(
            initialProfiles = emptyList()
        )

        val dnd = FakeDndGateway(
            policyAccess = true
        )

        val scheduling = FakeSchedulingGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = dnd,
            schedulingGateway = scheduling
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertNull(scheduling.scheduledEvent)
    }

    @Test
    fun statusNotificationDisabledCancelsNotification() = runTest {
        val repository = FakeRepository(
            initialProfiles = emptyList(),
            initialAppSettings = AppSettings(
                showStatusNotification = false
            )
        )

        val notification =
            FakeStatusNotificationGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = FakeDndGateway(
                policyAccess = true
            ),
            schedulingGateway = FakeSchedulingGateway(),
            statusNotificationGateway = notification
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertTrue(notification.cancelCalled)
        assertFalse(notification.inactiveShown)
        assertNull(notification.activeProfileName)
        assertNull(notification.nextEventProfileName)
    }

    @Test
    fun statusNotificationShowsInactiveWhenNoProfilesEnabled() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = false,
                    schedules = listOf(
                        Schedule(
                            daysOfWeek = setOf(DayOfWeek.MONDAY),
                            startMinute = 9 * 60,
                            endMinute = 17 * 60
                        )
                    )
                )
            ),
            initialAppSettings = AppSettings(
                showStatusNotification = true
            )
        )

        val notification =
            FakeStatusNotificationGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = FakeDndGateway(
                policyAccess = true
            ),
            schedulingGateway = FakeSchedulingGateway(),
            statusNotificationGateway = notification
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertTrue(notification.inactiveShown)
        assertNull(notification.activeProfileName)
        assertNull(notification.nextEventProfileName)
    }

    @Test
    fun statusNotificationShowsActiveProfileAndEndTime() = runTest {
        val repository = FakeRepository(
            initialProfiles = listOf(
                Profile(
                    id = "work",
                    name = "Work",
                    enabled = true,
                    schedules = listOf(
                        Schedule(
                            daysOfWeek = setOf(DayOfWeek.MONDAY),
                            startMinute = 9 * 60,
                            endMinute = 17 * 60
                        )
                    )
                )
            ),
            initialAppSettings = AppSettings(
                showStatusNotification = true
            )
        )

        val notification =
            FakeStatusNotificationGateway()

        val coordinator = QuietScheduleCoordinator(
            repository = repository,
            dndGateway = FakeDndGateway(
                policyAccess = true
            ),
            schedulingGateway = FakeSchedulingGateway(),
            statusNotificationGateway = notification
        )

        coordinator.reconcile(
            LocalDateTime.of(2026, 8, 10, 12, 0)
        )

        assertEquals(
            "Work",
            notification.activeProfileName
        )

        assertEquals(
            LocalTime.of(17, 0),
            notification.activeEndTime
        )

        assertFalse(notification.inactiveShown)
        assertNull(notification.nextEventProfileName)
    }

    @Test
    fun statusNotificationShowsNextEventWhenProfileEnabledButNotActive() =
        runTest {

            val repository = FakeRepository(
                initialProfiles = listOf(
                    Profile(
                        id = "work",
                        name = "Work",
                        enabled = true,
                        schedules = listOf(
                            Schedule(
                                daysOfWeek =
                                    setOf(DayOfWeek.MONDAY),
                                startMinute =
                                    9 * 60,
                                endMinute =
                                    17 * 60
                            )
                        )
                    )
                ),
                initialAppSettings = AppSettings(
                    showStatusNotification = true
                )
            )

            val notification =
                FakeStatusNotificationGateway()

            val coordinator = QuietScheduleCoordinator(
                repository = repository,
                dndGateway = FakeDndGateway(
                    policyAccess = true
                ),
                schedulingGateway = FakeSchedulingGateway(),
                statusNotificationGateway = notification
            )

            coordinator.reconcile(
                LocalDateTime.of(2026, 8, 10, 8, 0)
            )

            assertEquals(
                "Work",
                notification.nextEventProfileName
            )

            assertEquals(
                LocalDateTime.of(
                    2026,
                    8,
                    10,
                    9,
                    0
                ),
                notification.nextEventDateTime
            )

            assertFalse(notification.inactiveShown)
            assertNull(notification.activeProfileName)
        }

    @Test
    fun missingPolicyAccessWithStatusNotificationEnabledShowsInactive() =
        runTest {

            val repository = FakeRepository(
                initialProfiles = listOf(
                    Profile(
                        id = "work",
                        name = "Work",
                        enabled = true,
                        schedules = listOf(
                            Schedule(
                                daysOfWeek =
                                    setOf(DayOfWeek.MONDAY),
                                startMinute =
                                    9 * 60,
                                endMinute =
                                    17 * 60
                            )
                        )
                    )
                ),
                initialAppSettings = AppSettings(
                    showStatusNotification = true
                )
            )

            val notification =
                FakeStatusNotificationGateway()

            val coordinator = QuietScheduleCoordinator(
                repository = repository,
                dndGateway = FakeDndGateway(
                    policyAccess = false
                ),
                schedulingGateway = FakeSchedulingGateway(),
                statusNotificationGateway = notification
            )

            coordinator.reconcile(
                LocalDateTime.of(2026, 8, 10, 12, 0)
            )

            assertTrue(
                repository.savedProfiles.all {
                    !it.enabled
                }
            )

            assertTrue(notification.inactiveShown)
            assertNull(notification.activeProfileName)
            assertNull(notification.nextEventProfileName)
        }

    private class FakeRepository(
        initialProfiles: List<Profile>,
        initialAppSettings: AppSettings =
            AppSettings()
    ) : QuietScheduleRepositoryContract {

        private val profilesFlow =
            MutableStateFlow(
                initialProfiles
            )

        private val appSettingsFlow =
            MutableStateFlow(
                initialAppSettings
            )

        override val profiles:
                Flow<List<Profile>>
            get() = profilesFlow

        override val appSettings:
                Flow<AppSettings>
            get() = appSettingsFlow

        var savedProfiles:
                List<Profile> = initialProfiles
            private set

        override suspend fun saveProfiles(
            profiles: List<Profile>
        ) {
            savedProfiles = profiles
            profilesFlow.value = profiles
        }

        override suspend fun setShowStatusNotification(
            enabled: Boolean
        ) {
            appSettingsFlow.value =
                AppSettings(
                    showStatusNotification =
                        enabled
                )
        }
    }

    private class FakeDndGateway(
        private val policyAccess: Boolean
    ) : DndGateway {

        var priorityModeApplied =
            false
            private set

        var dndDisabled =
            false
            private set

        var appliedPolicy:
                DndPolicy? = null
            private set

        override fun hasPolicyAccess(): Boolean {
            return policyAccess
        }

        override fun applyPriorityMode(
            policy: DndPolicy
        ) {
            priorityModeApplied = true
            appliedPolicy = policy
        }

        override fun disableDnd() {
            dndDisabled = true
        }
    }

    private class FakeStatusNotificationGateway :
        StatusNotificationGateway {

        var activeProfileName:
                String? = null
            private set

        var activeEndTime:
                LocalTime? = null
            private set

        var nextEventProfileName:
                String? = null
            private set

        var nextEventDateTime:
                LocalDateTime? = null
            private set

        var inactiveShown =
            false
            private set

        var cancelCalled =
            false
            private set

        override fun showActive(
            profileName: String,
            endTime: LocalTime
        ) {
            activeProfileName =
                profileName

            activeEndTime =
                endTime
        }

        override fun showNextEvent(
            profileName: String,
            dateTime: LocalDateTime
        ) {
            nextEventProfileName =
                profileName

            nextEventDateTime =
                dateTime
        }

        override fun showInactive() {
            inactiveShown = true
        }

        override fun cancel() {
            cancelCalled = true
        }
    }
}

private class FakeSchedulingGateway :
    SchedulingGateway {

    var scheduledEvent:
            ScheduleEvent? = null
        private set

    var cancelAllCalled =
        false
        private set

    override fun scheduleNext(
        event: ScheduleEvent?
    ) {
        scheduledEvent = event
    }

    override fun cancelAll() {
        cancelAllCalled = true
    }
}