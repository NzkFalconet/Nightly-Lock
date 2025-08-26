package com.example.androidnightblocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat

class BlockVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val CHANNEL_ID = "vpn_blocker_channel"

    companion object {
        const val ACTION_STOP_VPN = "com.example.androidnightblocker.ACTION_STOP_VPN"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_VPN) {
            Log.d("NightBlocker", "BlockVpnService: STOP command received.")
            stopBlocking()
            stopSelf()
            return Service.START_NOT_STICKY
        }

        val notification = buildNotification("Internet blocked — until morning")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
        
        startBlocking()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("NightBlocker", "BlockVpnService: onDestroy called. Shutting down VPN.")
        stopBlocking()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    private fun startBlocking() {
        stopBlocking() // Ensure no old interface is running
        val builder = Builder()
            .setSession("AndroidNightBlocker")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)

        vpnInterface = builder.establish()
        if (vpnInterface == null) {
            Log.e("NightBlocker", "VPN establish() returned null. Stopping service.")
            stopSelf()
        }
    }

    private fun stopBlocking() {
        vpnInterface?.close()
        vpnInterface = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(CHANNEL_ID, "VPN Blocker", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification {
        val stopIntent = Intent(this, BlockVpnService::class.java).apply {
            action = ACTION_STOP_VPN
        }
        val pStop = PendingIntent.getService(
            this, 2001, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Android Night Blocker")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification_key) // Using our new key icon
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pStop)
            .build()
    }
}