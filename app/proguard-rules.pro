# Media3 / ExoPlayer and Room generate code reflectively in places; keep it simple
# and safe for a personal-use build rather than chasing minification edge cases.
-keep class androidx.media3.** { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.media3.**
