# Pallankuzhi Android App (Samsung Galaxy M32 5G friendly)

This project is a native Android app (Kotlin + Jetpack Compose) for the traditional Tamil game **Pallankuzhi / Pallankuli**.

## What is implemented
- 2 rows × 7 columns board (14 cups).
- Total 148 shells distributed at start (first 8 cups have 11, next 6 cups have 10).
- Local two-player gameplay on one phone.
- Relay sowing flow (if final shell lands in a non-empty cup, continue sowing from there).
- Capture rule: if final shell lands in your own empty cup and opposite cup is non-empty, capture both.
- End game when one row becomes empty; remaining shells are awarded and winner is declared.
- Board style switcher: rectangular board or fish-like rounded board.
- Wood-tone base color for the board.

## Device target
- Minimum Android SDK: 24 (Android 7.0)
- Target SDK: 35
- Samsung Galaxy M32 5G can run this app.

## Build
Open the folder in Android Studio and run the `app` configuration.

> Note: Building in restricted CI/container environments may fail if Google Maven is blocked.
