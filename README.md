# NotifyWake

Lights up your phone's screen when a new notification arrives, like an iPhone does. The lock screen
shows the notification and **the phone stays locked**: PIN, password and fingerprint are still
required. No root.

**Designed & built by Tamim Ahmed Rijan.**

- Kotlin + Jetpack Compose (Material 3), minSdk 26 (Android 8.0), targetSdk 35 (Android 15)
- APK: `../release/NotifyWake-1.1.0.apk` (about 1 MB)

The app has four tabs in a Material 3 navigation bar:

| Tab | What's there |
|---|---|
| **Home** | Main on/off switch with live status, permission summary, at-a-glance settings, Test button, activity |
| **Settings** | Which apps can wake the screen, cooldown, screen-on time, filters, backup wake method |
| **Permissions** | Progress bar plus a checklist of required and recommended permissions, with a button for each |
| **Tips** | Samsung battery setup, troubleshooting, privacy & security, About |

> **Upgrading from 1.0.0:** version 1.1.0 is signed with a new, personal release key, so Android won't
> install it over 1.0.0. **Uninstall the old NotifyWake first**, then install 1.1.0 and grant
> the permissions again. Future versions signed with this key will update in place.

---

## 1. Install the APK on your Samsung phone

1. Copy `NotifyWake-1.0.0.apk` to the phone (USB cable, Quick Share, Google Drive, or email it to yourself).
2. On the phone, open **My Files** → find the APK → tap it.
3. If asked, allow **Install unknown apps** for My Files (or whichever app you opened it from), then go back and tap **Install**.
4. If Google Play Protect shows "Unsafe app blocked" / "Scan app", tap **More details → Install anyway**
   (see *About Play Protect* below).

(With USB debugging on, you can also install from the PC: `adb install -r NotifyWake-1.0.0.apk`.)

### About Play Protect

Play Protect warns about **any** app installed from an APK that Google hasn't seen before. No app
can switch this warning off for itself, and NotifyWake doesn't try to. What 1.1.0 does to look less
suspicious:

- It's signed with a personal release key instead of the shared "Android Debug" key.
- It no longer asks for `QUERY_ALL_PACKAGES` (the "see all installed apps" permission). The app
  picker now lists apps that have a launcher icon.

The warning may still appear on install or update. Tapping **Install anyway** is safe here: you
built this app yourself. If you want Google to stop flagging it, you can submit the APK for review
through Google's Play Protect appeals form (search for "Play Protect appeal"). You can also turn
off scanning yourself in Play Store → profile → Play Protect → ⚙ → *Scan apps with Play Protect*,
but this isn't recommended: it turns off protection for every app on the phone.

## 2. Grant permissions (in the app's Permissions tab)

Open **NotifyWake**. Every item has a button that takes you to the correct settings screen.

| Item | Needed? | What to do |
|---|---|---|
| **Notification access** | Required | Tap *Grant access* → turn on **NotifyWake** → Allow. |
| **Unrestricted battery** | Strongly recommended | Tap *Allow* → **Allow** in the dialog. |
| **Show notifications** | Recommended | Tap *Allow*. Used for the test and the backup wake method. |
| **Full-screen notifications** (Android 14+) | Recommended | Tap *Allow* → turn on. Used by the backup wake method. |
| **Turn screen on** (only shown if Android blocks it) | Required if shown | App info → Special access / Advanced → allow *Turn screen on*. |

**"Restricted setting" / switch greyed out?** Android 13+ blocks sideloaded apps from Notification
access until you allow it:
Settings → Apps → NotifyWake → **⋮** (top right) → **Allow restricted settings** → confirm with your
PIN/fingerprint, then grant Notification access again. (On some One UI versions the ⋮ menu only
appears *after* you try to enable access once and get the "Restricted setting" message.)

## 3. Samsung One UI battery settings (do these once)

One UI aggressively freezes background apps. The app's **Samsung** card has buttons for these.

1. **Settings → Apps → NotifyWake → Battery → Unrestricted.**
2. **Settings → Battery** (older: *Device care → Battery*) **→ Background usage limits**:
   - Make sure NotifyWake is **not** in *Sleeping apps* or *Deep sleeping apps*.
   - Add NotifyWake to **Never sleeping apps**.
   - Optionally turn off **Put unused apps to sleep**.
3. **Lock it in Recents:** open Recents, tap the NotifyWake icon above its card → **Keep open**.
   Then "Close all" won't stop it.
4. **Settings → Apps → NotifyWake:** turn off *Pause app activity if unused* /
   *Remove permissions if app is unused* if you see it.
5. **Settings → Lock screen → Notifications:** make sure notifications are shown on the lock screen
   (otherwise the screen lights up with nothing on it).
6. If you use **Power saving**, Samsung may limit background apps more; test with it on and off.

## 4. Test it

1. In the app, make sure the top card says **Active**.
2. In **Test it**, tap **Start test**.
3. Press the side button to turn the screen off and wait.
4. About 8 seconds later a "NotifyWake test" notification arrives, and **the screen should light up
   on the lock screen**.
5. Then try a real one: send yourself a WhatsApp message or email from another device.

The **Activity** card shows how many times the screen was woken and which app did it last.

## 5. Choosing apps

**Which apps can wake the screen:**
- **All apps** (default): filtering is off, every app can wake the screen.
- **Selected apps**: in the Settings tab, tap **Choose apps**, then search and tick WhatsApp,
  Messenger, Gmail, etc. *Select all* / *Clear* act on the apps currently listed (respecting the search).

## 6. Behavior options

| Option | Default | Meaning |
|---|---|---|
| Cooldown between wakes | Off | After a wake, ignore further notifications for 5 s – 5 min. Good for chatty group chats. |
| Keep screen on for at least | 5 s | How long the screen is held on. After that the normal lock-screen timeout applies. |
| Ignore silent notifications | On | Skip low-priority notifications that don't sound or pop up. |
| Ignore ongoing notifications | On | Skip music players, downloads, navigation, "app is running" notices. |
| Respect Do Not Disturb | On | Don't wake for notifications that DND / Sleep mode silences. |
| Backup wake method | On | If the screen is still off 0.7 s after the normal wake, use a silent full-screen alert to wake it. |

Always on, no setting: the screen never wakes **during a phone call** (avoids ear presses). It also
doesn't wake when the screen is already on, or when an existing notification updates silently
(e.g. a download progress bar).

---

## How it works

- **Detecting notifications:** `WakeListenerService` is a `NotificationListenerService`. Once you grant
  Notification access, **Android itself** binds and keeps it running. That's why it survives closing
  the app or swiping it from Recents, and why Android re-binds it after a reboot (once you unlock the
  phone for the first time). `BootReceiver` and `onListenerDisconnected()` also call `requestRebind()`
  in case One UI is slow to do it.
- **Waking the screen (primary):** a short, timed `SCREEN_BRIGHT_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP`.
  This turns the display on to the normal lock screen. The `TURN_SCREEN_ON` permission is declared
  for Android 14+.
- **Waking the screen (backup):** some firmware ignores the wake-lock flag. If the screen is still off,
  the app posts a silent, high-priority notification with a **full-screen intent** that opens an
  invisible activity (`setTurnScreenOn(true)` + `setShowWhenLocked(true)`). That activity finishes
  within half a second and cancels its notification. It **never** calls `requestDismissKeyguard`,
  so the phone stays locked.
- **Battery:** no polling, no foreground service, no permanent notification. The app only runs code
  when Android delivers a notification, and every wake lock is time-limited so it can't keep the
  screen on.
- **Privacy:** notification content is never read, stored or sent. Only the package name and time of
  the last wake are kept, for the Activity card. The app has no internet permission.

## Limitations (honest list)

- Android doesn't guarantee that a third-party app can wake the screen on every device or firmware.
  That's why there's a backup method and a Test button. If both fail on your phone, check the
  checklist and Samsung steps first.
- After a restart, nothing runs until you unlock the phone once (Android's Direct Boot rule).
- An app's notifications only wake the screen if that app actually delivers them. If Samsung has put
  WhatsApp (etc.) to deep sleep, its notifications arrive late, so put those apps in *Never sleeping
  apps* too.
- If Samsung's **"Always On Display"** is enabled, it still works: the screen fully lights up from AOD.

## Troubleshooting

| Symptom | Fix |
|---|---|
| Top card says "Starting…" for a long time | Turn Notification access off and on again, or restart the phone. |
| Test notification appears but screen stays dark | Allow *Full-screen notifications* and keep *Backup wake method* on. Check the *Turn screen on* item. |
| Works for a while, then stops | Samsung put it to sleep. Redo section 3 (Unrestricted, Never sleeping apps, Keep open). |
| Screen wakes too often | Turn on a cooldown, use *Selected apps*, keep *Ignore silent notifications* on. |
| Wakes at night | Use Samsung's Sleep mode / DND with *Respect Do Not Disturb* on. |

Debug log: `adb logcat -s NotifyWake` shows every decision ("Waking screen for …" / "Not waking for …: reason").

---

## Building from source

Requirements: JDK 17, Android SDK (platform 35, build-tools 34). Android Studio Ladybug or newer
opens the project directly.

```bash
gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`.

**Signing key:** release builds are signed with `keystore/notifywake-release.jks`, and its password is in
`keystore.properties`. Both are git-ignored. **Back up both files somewhere safe** (e.g. a private
cloud folder). Every future update must be signed with this exact key, or it won't install over the
existing app. If `keystore.properties` is missing, the build falls back to the debug key.

## Project layout

```
app/src/main/java/com/notifywake/app/
├── data/SettingsStore.kt            settings (SharedPreferences)
├── service/WakeListenerService.kt   receives notifications, decides whether to wake
├── service/ScreenWaker.kt           wake lock + full-screen-intent backup
├── service/WakeActivity.kt          invisible activity used by the backup path
├── service/BootReceiver.kt          re-binds the listener after boot / update
├── service/TestNotifier.kt          delayed test notification
├── util/SystemStatus.kt             permission checks + settings shortcuts
└── ui/                              Compose UI: MainActivity (navigation bar), HomeTab, SettingsTab,
                                     PermissionsTab, TipsTab, AppPickerScreen, Components, Theme
```

---

Made by **Tamim Ahmed Rijan**.
