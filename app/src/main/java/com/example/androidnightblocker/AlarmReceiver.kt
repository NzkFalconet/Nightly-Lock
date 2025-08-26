package com.example.androidnightblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NightBlocker", "AlarmReceiver: Woke up with action: ${intent.action}")

        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val startHour = prefs.getInt("start_hour", 22)
        val startMinute = prefs.getInt("start_minute", 0)
        val stopHour = prefs.getInt("stop_hour", 7)
        val stopMinute = prefs.getInt("stop_minute", 0)

        when (intent.action) {
            Scheduler.START_ACTION -> {
                val serviceIntent = Intent(context, BlockVpnService::class.java)
                context.startForegroundService(serviceIntent)
                Log.d("NightBlocker", "AlarmReceiver: Started blocker service.")
                // Reschedule for the next day
                Scheduler.scheduleDailyAlarms(context, startHour, startMinute, stopHour, stopMinute)
            }
            Scheduler.STOP_ACTION -> {
                val serviceIntent = Intent(context, BlockVpnService::class.java).apply {
                    action = BlockVpnService.ACTION_STOP_VPN
                }
                context.startService(serviceIntent)
                Log.d("NightBlocker", "AlarmReceiver: Sent stop command to service.")
                // Reschedule for the next day
                Scheduler.scheduleDailyAlarms(context, startHour, startMinute, stopHour, stopMinute)
            }
        }
    }
}