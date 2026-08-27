# Grind House Focus (Android wrapper app)

A native Android shell around your existing Grind House web app, plus a
Focus Mode that blocks a chosen list of apps for a set timer using an
Accessibility Service.

## Building it WITHOUT installing Android Studio (recommended)
This repo includes a GitHub Actions workflow (`.github/workflows/build.yml`)
that builds the APK entirely on GitHub's servers. Your computer never needs
to run Android Studio or the Android SDK.

1. Create a new **GitHub repository** (can be private) and push everything
   in this folder to it — either via the GitHub website's "upload files"
   drag-and-drop (simplest, no git command line needed), or `git push` if
   you're comfortable with that.
2. Go to the repo's **Actions** tab. A workflow called "Build APK" should
   already be listed (it triggers automatically on push to `main`, or click
   **Run workflow** to trigger it manually).
3. Wait for the green checkmark (a few minutes).
4. Click into the finished run → scroll to **Artifacts** →
   download `grind-house-focus-debug-apk`. That's a zip containing
   `app-debug.apk`.
5. Transfer that `.apk` to your phone (Drive, email to yourself, USB,
   WhatsApp to yourself, whatever's easiest) and open it to install. You'll
   need to allow "Install unknown apps" for whichever app you use to open
   the file — Android will prompt you for this the first time.

Repeat step 4-5 whenever you push a change and want an updated APK.

## Building it locally instead (if you do want Android Studio)
1. Install [Android Studio](https://developer.android.com/studio) —
   heads up, it wants ~8GB+ RAM and a few GB of disk space, so the cloud
   build above is the lighter option if your machine is older/low-spec.
2. **File → Open** → select this `GrindHouseApp` folder.
3. Let it sync Gradle (first sync needs internet, takes a few minutes).
4. Plug your phone in via USB with Developer Options + USB debugging
   enabled, or use an emulator, and click **Run ▶**.

## What's in here
- `MainActivity` — WebView loading your site (`siteUrl` in `MainActivity.kt`
  — currently set to `https://dishu-21.github.io/grind-house/`, change if
  it moves).
- `FocusModeActivity` — pick a duration (25/50/90 min) and which installed
  apps to block, then starts the session.
- `BlockAccessibilityService` — watches for app switches; if the app that
  just opened is on the blocklist and a session is active, it immediately
  redirects to `BlockedActivity`.
- `BlockedActivity` — full-screen "focus session active, X min left" screen
  shown instead of the blocked app.
- `FocusForegroundService` — persistent notification with time remaining
  and an "End session" button, so it's never silently running.
- `FocusSessionManager` — the one place session state (end time + blocked
  app list) lives, backed by SharedPreferences.

## First-time setup on each phone
1. Open the app once — it'll load Grind House in the WebView normally
   (nothing blocked yet).
2. Tap the "Focus Mode" toolbar button.
3. Pick a duration and check the apps to block.
4. Tap **Start Focus Session** — Android will ask you to enable the
   Accessibility permission for "Grind House Focus" the first time. Toggle
   it on in Settings, then come back and tap Start again.
5. You'll see the persistent notification counting down. Opening a blocked
   app during the session immediately bounces you to the "Focus session
   active" screen with a button back to Grind House.

## Known limitations / things to know
- **Only blocks apps, not phone calls, texts, or system UI** — those are
  deliberately excluded so people can still be reached in an emergency.
- **Not uninstall-proof.** Anyone can go to Settings → Apps → uninstall
  Grind House Focus, or turn off the Accessibility permission, to escape
  a block. There's no way around this without Device Owner/MDM enrollment,
  which is a much heavier lift and not appropriate for personal phones.
- **The block is per-device, not synced.** Starting a focus session on
  your phone doesn't affect anyone else's — each person runs their own.
- **Icon is a placeholder** — `res/drawable/ic_launcher.xml` is a simple
  vector flame. Swap in a real launcher icon via Android Studio's
  Image Asset tool (right-click `res` → New → Image Asset) whenever you
  want a nicer one, or leave it — it still builds fine as-is.
- If Grind House's URL or hosting changes, update `siteUrl` in
  `MainActivity.kt`.

