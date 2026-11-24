# Aura

A personal, ad-free media player for Android. Built with Kotlin, Jetpack Compose,
and Media3 (ExoPlayer). It only ever plays audio and video that is already on
your device — it requests **no internet permission at all**, so there is
nowhere for ads, trackers, or telemetry to come from.

**Lead Dev:** Hamza Sami

## Features

- Scans your device's own music and videos via MediaStore — nothing is downloaded or streamed
- Songs / Albums / Artists / Folders / Videos browsing
- Background playback with lock-screen / notification controls (Media3 MediaSession)
- Shuffle, repeat (off / all / one), playback speed (0.5x–2x)
- Queue screen: tap to jump, reorder, remove
- Favorites and unlimited custom playlists (stored locally in a Room database)
- 5-band equalizer with Bass Boost and four presets (Flat / Bass / Treble / Vocal)
- Sleep timer (fixed duration or "end of current track")
- Search across titles, artists, and albums
- Glass-morphism dark UI with a single accent color, art-forward album/playlist tiles
- Optional pure-AMOLED-black theme toggle
- Zero ads, zero accounts, zero network permission

## Project structure

```
app/src/main/java/com/sunsoft/aura/
  data/            MediaStore scanning, Room (playlists/favorites), Coil art fetcher
  playback/        MediaSessionService + the controller wrapper the UI talks to
  equalizer/        System Equalizer/BassBoost wrapper
  ui/theme/        Colors, type scale, Material3 theme
  ui/components/   Shared composables (glass panel, song rows, mini-player, tiles)
  ui/screens/      One package per screen
  ui/navigation/   NavHost + bottom bar + mini-player wiring
  viewmodel/       LibraryViewModel, PlayerViewModel, PlaylistViewModel
```

## Known limitations (by design, to keep this a reliable first build)

- The lock-screen notification doesn't show embedded album art yet (the in-app
  Now Playing screen does). Wiring a custom `BitmapLoader` for the system
  notification is a natural next step if you want to take this further.
- Shuffle/repeat/speed aren't restored between app restarts yet (equalizer
  settings *are* persisted). Easy to add later via the existing `Prefs` class.
- The debug APK is signed with the default debug key, which is fine for
  installing on your own phone but not for publishing anywhere public.

## Building it yourself

This repo includes a GitHub Actions workflow (`.github/workflows/build.yml`)
that runs the unit tests and builds a debug APK on every push, with no local
Android Studio setup required. See the deployment guide for the exact steps.

If you do want to build locally with Android Studio instead: open the project
folder, let Gradle sync, and run the `app` configuration, or
`./gradlew assembleDebug` from a terminal with the Android SDK installed.
