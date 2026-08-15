package io.github.elettrone2012.quietschedule.platform.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.elettrone2012.quietschedule.MainActivity
import io.github.elettrone2012.quietschedule.R
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class StatusNotificationManager(
    context: Context
) : StatusNotificationGateway {

    private val appContext =
        context.applicationContext

    private val notificationManager =
        appContext.getSystemService(
            NotificationManager::class.java
        )

    init {
        createChannel()
    }

    fun canPostNotifications(): Boolean {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun showActive(
        profileName: String,
        endTime: LocalTime
    ) {
        show(
            title =
                appContext.getString(
                    R.string.app_name
                ),
            text =
                appContext.getString(
                    R.string.status_active,
                    profileName,
                    formatTime(endTime)
                )
        )
    }

    override fun showNextEvent(
        profileName: String,
        dateTime: LocalDateTime
    ) {
        val locale =
            appLocale()

        val day =
            dateTime.dayOfWeek.getDisplayName(
                TextStyle.FULL,
                locale
            )

        show(
            title =
                appContext.getString(
                    R.string.app_name
                ),
            text =
                appContext.getString(
                    R.string.status_next_event,
                    profileName,
                    day,
                    formatTime(
                        dateTime.toLocalTime()
                    )
                )
        )
    }

    override fun showInactive() {
        show(
            title =
                appContext.getString(
                    R.string.quietschedule_inactive
                ),
            text = null
        )
    }

    override fun cancel() {
        notificationManager.cancel(
            NOTIFICATION_ID
        )
    }

    private fun show(
        title: String,
        text: String?
    ) {
        if (!canPostNotifications()) {
            return
        }

        val openAppIntent =
            Intent(
                appContext,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                appContext,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val builder =
            NotificationCompat.Builder(
                appContext,
                CHANNEL_ID
            )
                .setSmallIcon(
                    R.drawable.ic_notification
                )
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setPriority(
                    NotificationCompat.PRIORITY_LOW
                )

        if (!text.isNullOrBlank()) {
            builder.setContentText(text)
        }

        notificationManager.notify(
            NOTIFICATION_ID,
            builder.build()
        )
    }

    private fun formatTime(
        time: LocalTime
    ): String {
        val locale =
            appLocale()

        val pattern =
            if (
                DateFormat.is24HourFormat(
                    appContext
                )
            ) {
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

    private fun appLocale(): Locale {
        val locales =
            appContext.resources
                .configuration
                .locales

        return if (locales.isEmpty) {
            Locale.getDefault()
        } else {
            locales[0]
        }
    }

    private fun createChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                appContext.getString(
                    R.string.status_notification_channel
                ),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description =
                    appContext.getString(
                        R.string.status_notification_channel_description
                    )
            }

        notificationManager.createNotificationChannel(
            channel
        )
    }

    private companion object {
        const val CHANNEL_ID =
            "quietschedule_status"

        const val NOTIFICATION_ID =
            1002
    }
}