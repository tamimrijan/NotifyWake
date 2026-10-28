<div align="center">

# 🔔 NotifyWake

### Your screen lights up for every new notification, just like an iPhone.

**Samsung doesn't wake the screen for notifications. NotifyWake does, and keeps your phone locked.**

[![Release](https://img.shields.io/github/v/release/tamimrijan/NotifyWake?style=for-the-badge&color=4F5BD5&label=Release)](https://github.com/tamimrijan/NotifyWake/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/tamimrijan/NotifyWake/total?style=for-the-badge&color=1E8E3E&label=Downloads)](https://github.com/tamimrijan/NotifyWake/releases)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![No Root](https://img.shields.io/badge/No%20Root-Required-success?style=for-the-badge)

<br>

<a href="https://github.com/tamimrijan/NotifyWake/releases/latest">
  <img src="https://img.shields.io/badge/⬇%20Download%20APK-4F5BD5?style=for-the-badge&logoColor=white" height="48" alt="Download APK">
</a>

<br><br>

<img src="https://github.com/user-attachments/assets/9601ba46-5767-48f1-9d21-93f81507702d" width="23%" alt="NotifyWake screenshot 1">&nbsp;
<img src="https://github.com/user-attachments/assets/520501d9-b926-4655-acc6-bbd720e123b4" width="23%" alt="NotifyWake screenshot 2">&nbsp;
<img src="https://github.com/user-attachments/assets/db744515-1877-40e9-b220-c15852c45167" width="23%" alt="NotifyWake screenshot 3">&nbsp;
<img src="https://github.com/user-attachments/assets/1484d325-ba8d-4357-9c9b-eb1ef40e111e" width="23%" alt="NotifyWake screenshot 4">

<br>

**Designed & built by [Tamim Ahmed Rijan](https://github.com/tamimrijan)**

</div>

---

## ✨ Features

| | |
|---|---|
| 🔔 **Instant screen wake** | The screen lights up the moment a new notification arrives |
| 🔒 **Stays locked** | Only the display turns on. PIN, password and fingerprint are still required |
| 📋 **Pick your apps** | All apps, or only the ones you choose (WhatsApp, Messenger, Gmail…) |
| ⏱️ **Cooldown** | Stops a busy group chat from waking the screen again and again |
| 🌙 **Smart filters** | Respects Do Not Disturb, skips silent and ongoing notifications |
| 📞 **Call-safe** | Never wakes the screen during a phone call |
| 🛡️ **Backup wake method** | A second method for phones that block the first one |
| 🧪 **One-tap test** | Check that everything works in 8 seconds |
| 🔋 **Battery friendly** | No background polling and no permanent notification |
| 🕵️ **Private** | No internet permission. Notification content is never read or stored |
| 🎨 **Material 3 design** | Clean tabbed UI with dynamic colors and dark mode |

---

## 🚀 Quick start

1. **Download** `NotifyWake-1.1.0.apk` from [Releases](https://github.com/tamimrijan/NotifyWake/releases/latest).
2. **Install it.** Open the APK and allow *Install unknown apps* if asked.
3. **Open NotifyWake → Permissions tab** and tap each button.
4. **Samsung users:** follow the steps in the **Tips** tab (takes 1 minute).
5. **Home tab → Start test**, then turn the screen off. It should light up in 8 seconds. 🎉

> [!IMPORTANT]
> **Updating from 1.0.0?** Uninstall the old version first. 1.1.0 uses a new signing key, so Android
> won't install it over 1.0.0. Future updates will install normally.

> [!NOTE]
> **Play Protect warning?** It appears for any app installed from outside the Play Store. Tap
> **More details → Install anyway**. The full source code is right here for anyone to check.

---

## 📱 The app

| Tab | What's inside |
|---|---|
| 🏠 **Home** | Main on/off switch with live status, permission summary, quick settings, test button, activity |
| ⚙️ **Settings** | Choose apps, cooldown, screen-on time, filters, backup wake method |
| 🔐 **Permissions** | Progress bar and a checklist with a one-tap fix for each item |
| 💡 **Tips** | Samsung battery setup, troubleshooting, privacy info, About |

---

## 📖 Full setup guide

<details>
<summary><b>1. Install the APK</b></summary>
<br>

1. Copy `NotifyWake-1.1.0.apk` to your phone (USB, Quick Share, Google Drive, or download it from Releases on the phone).
2. Open **My Files** → find the APK → tap it.
3. If asked, allow **Install unknown apps** for My Files (or your browser), then tap **Install**.
4. If Play Protect shows *"Unsafe app blocked"*, tap **More details → Install anyway**.

With USB debugging on, you can also run `adb install -r NotifyWake-1.1.0.apk`.

</details>

<details>
<summary><b>2. Grant permissions</b></summary>
<br>

Open the **Permissions** tab. Every item has a button that opens the right settings screen.

| Permission | Needed? | Why |
|---|---|---|
| **Notification access** | Required | To notice new notifications |
| **Unrestricted battery** | Required | So One UI doesn't put the app to sleep |
| **Show notifications** | Recommended | For the test and the backup wake method |
| **Full-screen notifications** (Android 14+) | Recommended | For the backup wake method |
| **Turn screen on** (only shown if blocked) | Required if shown | Lets the app turn on the display |

**Notification access switch greyed out ("Restricted setting")?**
Android 13+ blocks this for apps installed from an APK. Go to Settings → Apps → NotifyWake →
**⋮** (top right) → **Allow restricted settings**, confirm with your PIN or fingerprint, then try again.

</details>

<details>
<summary><b>3. Samsung One UI battery settings</b></summary>
<br>

One UI aggressively freezes background apps. Do these once:

1. **Settings → Apps → NotifyWake → Battery → Unrestricted**
2. **Settings → Battery → Background usage limits**
   - Remove NotifyWake from *Sleeping apps* / *Deep sleeping apps*
   - Add it to **Never sleeping apps**
3. **Recents** → tap the NotifyWake icon → **Keep open**
4. **App info** → turn off *Pause app activity if unused*
5. **Settings → Lock screen → Notifications** → make sure notifications show on the lock screen

💡 If WhatsApp (or another app) notifications arrive late, add **that app** to *Never sleeping apps* too.

</details>

<details>
<summary><b>4. Behavior options</b></summary>
<br>

| Option | Default | What it does |
|---|---|---|
| Cooldown between wakes | Off | Ignore new notifications for 5 s – 5 min after a wake |
| Keep screen on for at least | 5 s | Minimum time the screen stays on |
| Ignore silent notifications | On | Skip low-priority notifications |
| Ignore ongoing notifications | On | Skip music, downloads, navigation |
| Respect Do Not Disturb | On | Stay dark when DND / Sleep mode is on |
| Backup wake method | On | Second method if the screen is still off after 0.7 s |

Always on: no wake during phone calls, no wake when the screen is already on, and no wake for silent updates (like download progress).

</details>

<details>
<summary><b>🛠️ Troubleshooting</b></summary>
<br>

| Problem | Fix |
|---|---|
| Status stuck on "Starting…" | Turn Notification access off and on, or restart the phone |
| Test notification shows but the screen stays dark | Allow *Full-screen notifications*, keep *Backup wake method* on |
| Works for a while, then stops | Samsung put it to sleep. Redo the battery settings above |
| Screen wakes too often | Set a cooldown or use *Selected apps* |
| Wakes at night | Use Sleep mode / DND with *Respect Do Not Disturb* on |

Debug log: `adb logcat -s NotifyWake` shows why the screen did or didn't wake for each notification.

</details>

---

## ⚙️ How it works

```
New notification
      │
      ▼
NotificationListenerService ──► filters (app list, DND, silent, call, cooldown)
      │
      ▼
Screen wake lock (ACQUIRE_CAUSES_WAKEUP)
      │
      ├── screen on? ──► ✅ lock screen shows the notification
      │
      └── still off? ──► backup: silent full-screen intent → invisible activity → ✅
```

- **Always running:** Android itself keeps the notification listener alive, even after you close
  the app or restart the phone.
- **Secure:** it never calls `requestDismissKeyguard`. The phone stays locked.
- **Efficient:** every wake lock has a time limit, with no polling and no foreground service.

<details>
<summary><b>Limitations</b></summary>
<br>

- Android doesn't guarantee that every phone allows third-party apps to wake the screen. That's why
  there's a backup method and a test button.
- After a restart, it starts working once you unlock the phone for the first time (Android's Direct Boot rule).
- It works with Always On Display too: the screen fully lights up from AOD.

</details>

---

## 🧑‍💻 Build from source

**Requirements:** JDK 17 and the Android SDK (platform 35). You can also open the project in Android Studio.

```bash
git clone https://github.com/tamimrijan/NotifyWake.git
cd NotifyWake
./gradlew assembleRelease
```

The APK is created at `app/build/outputs/apk/release/`. Without your own `keystore.properties`, the
build is signed with the debug key.

<details>
<summary><b>Project structure</b></summary>
<br>

```
app/src/main/java/com/notifywake/app/
├── data/SettingsStore.kt            settings storage
├── service/WakeListenerService.kt   receives notifications, decides whether to wake
├── service/ScreenWaker.kt           wake lock + full-screen-intent backup
├── service/WakeActivity.kt          invisible activity for the backup method
├── service/BootReceiver.kt          reconnects after reboot / update
├── service/TestNotifier.kt          test notification
├── util/SystemStatus.kt             permission checks + settings shortcuts
└── ui/                              Material 3 Compose UI (tabs, app picker, theme)
```

</details>

---

<div align="center">

### 👨‍💻 Developer

**Tamim Ahmed Rijan**

[![GitHub](https://img.shields.io/badge/GitHub-tamimrijan-181717?style=for-the-badge&logo=github)](https://github.com/tamimrijan)

If NotifyWake helped you, please give it a ⭐. It really helps!

<sub>Made with ❤️ using Kotlin & Jetpack Compose</sub>

</div>
