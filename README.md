Digital Curfew for Android
An Android app to automatically schedule internet downtime for focus and digital wellness.

Digital Curfew is your simple, powerful tool to create a distraction-free environment by automatically scheduling your internet downtime. Set it once, and let the app handle the rest.

✨ Features
✓ Automatic Scheduling: Set your "unplug" and "reconnect" times and forget about it. The app handles everything automatically.

✓ Secure Local VPN: Uses a secure, on-device VpnService to create a local firewall. It does not send your data anywhere—your privacy is guaranteed.

✓ Manual Override: Need to end the block early? The "Stop" button in the app gives you full control.

✓ Survives Reboots: Your schedule is automatically re-armed whenever you restart your device.

✓ Battery Friendly: Designed to be lightweight and efficient, with no impact on your battery life.

🎯 Perfect For
😴 Improving Sleep Hygiene: Stop "just one more video" from turning into a late night.

📚 Students: Create a distraction-free zone to focus on studying and homework.

👪 Parents: Enforce a consistent "internet off" time for your children's devices.

🧘 Digital Wellness: Take a daily break from notifications and the pressure to be online.

🛠️ How It Works (Technical Overview)
This app uses a combination of core Android APIs to achieve its functionality reliably and efficiently:

VpnService: The core of the blocker. The app creates a local-only VPN tunnel. Instead of routing traffic to an external server, it simply blocks all outgoing network packets, effectively shutting down internet access without monitoring your data.

AlarmManager: Used to schedule the exact times for the VPN to start and stop. It uses setExactAndAllowWhileIdle() to ensure the tasks run even when the device is in Doze mode.

BroadcastReceiver: Two receivers are used:

An AlarmReceiver listens for the scheduled alarms to trigger the start/stop actions.

A BootReceiver listens for the BOOT_COMPLETED action to automatically reschedule the alarms if the phone is restarted.

Foreground Service: The VpnService runs as a foreground service with a persistent notification, which is required by modern Android versions to prevent the system from killing long-running background tasks.

🚀 Getting Started
Clone this repository.

Open the project in the latest version of Android Studio.

Build the project and run it on an emulator or a physical device.

📜 License
This project is licensed under the MIT License - see the LICENSE.md file for details.
