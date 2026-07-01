# TickTock

A minimalist Android app that announces the current time aloud at regular intervals within a user-defined time window.

## Features

- **Frequency**: Choose how often the time is announced — every 10, 20, 30, or 60 minutes
- **Time range**: Set a start and end time (e.g. 6:00 AM – 8:00 AM)
- **Multiple profiles**: Create as many alert profiles as you need
- **Delete profiles**: Remove profiles you no longer need
- **Loud announcements**: Uses the alarm audio stream at maximum volume so the time is spoken clearly through the speaker

## How it works

1. Tap **+** to create a new time alert profile
2. Pick a frequency and active time range, then tap **Save**
3. The app schedules alarms at each interval within your range (e.g. 6:00, 6:10, 6:20 … 8:00 for a 10-minute frequency between 6 AM and 8 AM)
4. At each scheduled time, the app speaks the current time aloud
5. Alerts persist across device reboots

## Build

Requires Android SDK (API 35) and JDK 17+.

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Requirements

- Android 8.0 (API 26) or higher
- On Android 12+, grant the **Alarms & reminders** permission when prompted so exact-time alerts work reliably
