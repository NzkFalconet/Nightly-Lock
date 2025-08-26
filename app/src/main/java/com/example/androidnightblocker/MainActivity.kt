package com.example.androidnightblocker

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var txtStatus: TextView
    private lateinit var txtStartTime: TextView
    private lateinit var txtEndTime: TextView

    private var startHour = 22 // Default to 10 PM
    private var startMinute = 0
    private var stopHour = 7   // Default to 7 AM
    private var stopMinute = 0

    // This launcher now handles both requesting VPN permission and re-triggering the
    // save/schedule button's logic after the user grants permission.
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "VPN Permission Granted!", Toast.LENGTH_SHORT).show()
            // After getting VPN permission, we "click" the save button again programmatically
            // to continue the scheduling process.
            findViewById<Button>(R.id.btnSaveSchedule).performClick()
        } else {
            Toast.makeText(this, "Permission is required for the blocker.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find all UI elements
        txtStatus = findViewById(R.id.txtStatus)
        txtStartTime = findViewById(R.id.txtStartTime)
        txtEndTime = findViewById(R.id.txtEndTime)
        val btnSetStartTime = findViewById<Button>(R.id.btnSetStartTime)
        val btnSetEndTime = findViewById<Button>(R.id.btnSetEndTime)
        val btnSaveSchedule = findViewById<Button>(R.id.btnSaveSchedule)
        val btnCancelSchedule = findViewById<Button>(R.id.btnCancelSchedule)
        val btnStop = findViewById<Button>(R.id.stopButton)
        val btnTestNow = findViewById<Button>(R.id.testNowButton)

        loadTimes()
        updateTimeTextViews()

        btnSetStartTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                startHour = hourOfDay
                startMinute = minute
                updateTimeTextViews()
            }, startHour, startMinute, false).show()
        }

        btnSetEndTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                stopHour = hourOfDay
                stopMinute = minute
                updateTimeTextViews()
            }, stopHour, stopMinute, false).show()
        }

        btnSaveSchedule.setOnClickListener {
            val vpnIntent = VpnService.prepare(this)
            if (vpnIntent != null) {
                // VPN permission is MISSING. Request it.
                // The vpnPermissionLauncher will handle what happens next.
                Toast.makeText(this, "Please grant VPN permission to set a schedule.", Toast.LENGTH_LONG).show()
                vpnPermissionLauncher.launch(vpnIntent)
                return@setOnClickListener // Stop further execution until permission is handled
            }

            // If we reach here, VPN permission IS granted. Now check for alarm permission.
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Alarm permission is MISSING. Request it.
                Toast.makeText(this, "Permission for exact alarms is needed.", Toast.LENGTH_LONG).show()
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            } else {
                // BOTH permissions are granted. We can now save and schedule.
                saveTimes()
                Scheduler.scheduleDailyAlarms(this, startHour, startMinute, stopHour, stopMinute)
                txtStatus.text = "Status: Schedule is ARMED"
                Toast.makeText(this, "Schedule Saved and Armed!", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelSchedule.setOnClickListener {
            Scheduler.cancelAlarms(this)
            txtStatus.text = "Status: Schedule is CANCELED"
            Toast.makeText(this, "Schedule Canceled!", Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            val stopIntent = Intent(this, BlockVpnService::class.java).apply {
                action = BlockVpnService.ACTION_STOP_VPN
            }
            startService(stopIntent)
            txtStatus.text = "Status: Blocker stopped manually"
        }

        btnTestNow.setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                startService(Intent(this, BlockVpnService::class.java))
                txtStatus.text = "Status: Test blocker started"
            }
        }
    }

    private fun loadTimes() {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        startHour = prefs.getInt("start_hour", 22)
        startMinute = prefs.getInt("start_minute", 0)
        stopHour = prefs.getInt("stop_hour", 7)
        stopMinute = prefs.getInt("stop_minute", 0)
    }

    private fun saveTimes() {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().apply {
            putInt("start_hour", startHour)
            putInt("start_minute", startMinute)
            putInt("stop_hour", stopHour)
            putInt("stop_minute", stopMinute)
            apply()
        }
    }

    private fun updateTimeTextViews() {
        txtStartTime.text = formatTime(startHour, startMinute)
        txtEndTime.text = formatTime(stopHour, stopMinute)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val isPm = hour >= 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (isPm) "PM" else "AM"
        return String.format("%d:%02d %s", displayHour, minute, amPm)
    }
}