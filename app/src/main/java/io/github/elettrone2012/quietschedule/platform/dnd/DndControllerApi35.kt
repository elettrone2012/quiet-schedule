package io.github.elettrone2012.quietschedule.platform.dnd

import android.app.AutomaticZenRule
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.service.notification.Condition
import androidx.annotation.RequiresApi
import io.github.elettrone2012.quietschedule.DndRuleConfigActivity
import io.github.elettrone2012.quietschedule.domain.model.DndPolicy

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class DndControllerApi35(
    context: Context
) : DndGateway {

    private val appContext =
        context.applicationContext

    private val notificationManager =
        appContext.getSystemService(NotificationManager::class.java)

    private val configurationActivity =
        ComponentName(
            appContext,
            DndRuleConfigActivity::class.java
        )

    private fun findRuleId(): String? {
        return notificationManager
            .automaticZenRules
            .entries
            .firstOrNull { (_, rule) ->
                rule.conditionId == CONDITION_ID
            }
            ?.key
    }

    override fun hasPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    override fun applyPriorityMode(
        policy: DndPolicy
    ) {
        val existingRuleId =
            findRuleId()

        val rule =
            AutomaticZenRule.Builder(
                RULE_NAME,
                CONDITION_ID
            )
                .setConfigurationActivity(
                    configurationActivity
                )
                .setEnabled(true)
                .setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                )
                .setZenPolicy(
                    policy.toZenPolicy()
                )
                .build()

        val ruleId =
            if (existingRuleId == null) {
                notificationManager.addAutomaticZenRule(
                    rule
                )
            } else {
                notificationManager.updateAutomaticZenRule(
                    existingRuleId,
                    rule
                )

                existingRuleId
            }

        notificationManager.setAutomaticZenRuleState(
            ruleId,
            Condition(
                CONDITION_ID,
                RULE_NAME,
                Condition.STATE_TRUE
            )
        )
    }

    override fun disableDnd() {
        val ruleId =
            findRuleId()
                ?: return

        notificationManager.setAutomaticZenRuleState(
            ruleId,
            Condition(
                CONDITION_ID,
                RULE_NAME,
                Condition.STATE_FALSE
            )
        )
    }

    private companion object {
        const val RULE_NAME =
            "QuietSchedule"

        val CONDITION_ID: Uri =
            Uri.parse(
                "condition://io.github.elettrone2012.quietschedule/active"
            )
    }
}