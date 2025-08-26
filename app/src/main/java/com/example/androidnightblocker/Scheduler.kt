package com.example.androidnightblocker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object Scheduler {

    const val START_ACTION = "com.example.androidnightblocker.ACTION_START_BLOCKING"
    const val STOP_ACTION = "com.example.androidnightblocker.ACTION_STOP_BLOCKING"
    private const val START_REQUEST_CODE = 101
    private const val STOP_REQUEST_CODE = 102

    fun scheduleDailyAlarms(context: Context, startHour: Int, startMinute: Int, stopHour: Int, stopMinute: Int) {
        scheduleAlarm(context, startHour, startMinute, START_ACTION, START_REQUEST_CODE)
        scheduleAlarm(context, stopHour, stopMinute, STOP_ACTION, STOP_REQUEST_CODE)
    }

    private fun scheduleAlarm(context: Context, hour: Int, minute: Int, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("NightBlocker", "Scheduled '$action' for: ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("NightBlocker", "Failed to schedule exact alarm. Is the permission missing?", e)
        }
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val startIntent = Intent(context, AlarmReceiver::class.java).apply { action = START_ACTION }
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply { action = STOP_ACTION }

        val startPendingIntent = PendingIntent.getBroadcast(
            context, START_REQUEST_CODE, startIntent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, STOP_REQUEST_CODE, stopIntent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (startPendingIntent != null) {
            alarmManager.cancel(startPendingIntent)
            startPendingIntent.cancel()
            Log.d("NightBlocker", "Canceled start alarm.")
        }
        if (stopPendingIntent != null) {
            alarmManager.cancel(stopPendingIntent)
            stopPendingIntent.cancel()
            Log.d("NightBlocker", "Canceled stop alarm.")
        }
    }
}