package com.example.androidnightblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("NightBlocker", "BootReceiver: Device booted. Rescheduling alarms.")
            
            // Load the saved times and reschedule
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val startHour = prefs.getInt("start_hour", 22)
            val startMinute = prefs.getInt("start_minute", 0)
            val stopHour = prefs.getInt("stop_hour", 7)
            val stopMinute = prefs.getInt("stop_minute", 0)

            Scheduler.scheduleDailyAlarms(context, startHour, startMinute, stopHour, stopMinute)
        }
    }
}