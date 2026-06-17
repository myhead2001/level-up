package com.sololeveling.systemfit.presentation.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderManager {
    private const val PREFS_NAME = "system_fit_reminders"
    private const val ALARM_REQUEST_CODE = 4004

    fun scheduleNextReminder(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean("reminder_enabled", false)
        val daysString = sharedPrefs.getString("reminder_days", "2,3,4,5,6") ?: "2,3,4,5,6"
        val hour = sharedPrefs.getInt("reminder_hour", 9)
        val minute = sharedPrefs.getInt("reminder_minute", 0)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel any existing alarm first
        alarmManager.cancel(pendingIntent)

        if (!enabled) return

        val activeDays = daysString.split(",")
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
            .toSet()

        if (activeDays.isEmpty()) return

        val triggerTime = getNextReminderTime(activeDays, hour, minute)
        if (triggerTime > 0L) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to non-exact if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    private fun getNextReminderTime(activeDays: Set<Int>, targetHour: Int, targetMinute: Int): Long {
        if (activeDays.isEmpty()) return 0L
        val now = Calendar.getInstance()
        
        for (dayOffset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (candidate.after(now)) {
                val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
                if (activeDays.contains(dayOfWeek)) {
                    return candidate.timeInMillis
                }
            }
        }
        return 0L
    }
}
