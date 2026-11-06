# LinarTV Open — personal fork

**This is a fork of [LinarTV Open](https://github.com/domlin92/linartv-launcher-open) by domlin92.**
It is not the original project and it is not maintained by the original author. Installation, credits and
license come from upstream and still apply; the changes listed below exist only in this repository.

Upstream is the place to go for the official releases, documentation and issue reports:
<https://github.com/domlin92/linartv-launcher-open>.

## What the app is

LinarTV is a home screen (launcher) for Android TV built around football — from the Polish IV liga to the
European top 5. One screen shows the clock, the weather for a chosen city and the next matches of one or two
clubs you follow, and keeps your favourite apps one click away. Plain Java, one activity, no Kotlin and no
Compose.

LinarTV replaces the home screen. It does not uninstall the TV manufacturer's apps or remove their
advertising and tracking services. If you also want to tidy up the TV's system apps,
[CleanYourTV](https://github.com/Gathin23/cleanyourtv) is a separate project you can use alongside it.

## What this fork changes

Three commits on top of upstream `main` (`2a17480`), 27 files, +1595 / −80.

### 1. Custom dock icons and a clearer app drawer

- Dock tiles can show an image you pick: an in-launcher browser of the **Download** folder, including
  subfolders, for **PNG, JPG, JPEG, WEBP and SVG** (vectors are rendered).
- A tile's background colour comes from a **12-colour RYB palette** in a single row, from red through yellow
  to blue and violet.
- Live preview, working **Enlarge / Shrink**, images stored on the device as PNG (max 512 px) in
  `files/custom_icons`. Storage permission is asked once on the first pick, with the system file picker as a
  fallback.
- The app drawer now maps focus to the dock: Down from tile N highlights tile N, Down out of column N returns
  to dock tile N, and Back/Up returns to the tile that opened the list.
- A selected app uses the same aura as the match and weather cards (no drop shadow), grows 10% and gets a
  2 px frame — black on a light app, white on a dark one.

### 2. RAM widget, buttons visibility and wallpapers in Launcher appearance

- A **RAM readout** (used / total) sits left of Watch TV, Inputs and Settings, drawn like the round buttons.
  The top-right group packs against the right edge, so hiding any of the buttons leaves no gap.
- A **Buttons visibility** panel in Settings with switches for the RAM widget, Watch TV and Inputs. The choices
  are stored on the device and applied when the panel closes.
- **Wallpapers** moved from their own settings tile into **Launcher appearance**; the freed tile is the new
  visibility panel.

### 3. Weather wallpaper — 7 kinds × day / night

- A **"Wallpaper follows the weather"** switch in the wallpaper gallery
  (Settings → Launcher appearance → Wallpapers): the home screen background shows the current Open-Meteo
  conditions of the chosen city.
- **Seven kinds** folded from the WMO codes the widget already uses — clear, partly cloudy, cloudy, rain,
  snow, thunderstorm, fog. Drizzle and showers count as rain, sleet as snow, hail as a thunderstorm and an
  unknown code as clouds, so nothing falls outside the seven.
- **Day and night** follow the launcher's own appearance mode (automatic schedule, always light, always dark,
  custom hours), so the background flips together with the rest of the interface — "always light" always shows
  the day variant.
- **Fourteen templates** in `app/src/main/res/drawable/weather_<kind>_<day|night>.xml`. They are meant to be
  replaced in the source code; the launcher cannot edit them. Change the gradient colours, or replace a file
  with your own 1920×1080 PNG / WEBP / JPG under the same base name — no code changes needed.
  See [docs/WEATHER-WALLPAPERS.md](docs/WEATHER-WALLPAPERS.md) (Polish and English).
- Picking a photo in the gallery turns the option off and keeps the photo. Without a forecast yet, the
  wallpaper chosen in the gallery stays visible.
- The logic lives in `WeatherWallpaper.java` (plain Java, no Android imports), covered by
  `tests/WeatherWallpaperCheck.java`.

## Build from source

JDK 17 and the Android SDK are required (`JAVA_HOME`, `ANDROID_HOME`).

```text
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/LinarTV-<version>-debug.apk`.
Local checks: `scripts/test-local.ps1` (Windows) or the Java checks in `tests/`.

## Install on a TV

The signed official APK is published by upstream in
[its Releases](https://github.com/domlin92/linartv-launcher-open/releases/latest). Builds from this fork are
debug builds — install them over `adb`:

```text
adb connect <TV_IP>:5555
adb install -r LinarTV-<version>-debug.apk
adb shell am start -n io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
```

To make it the home screen, use your TV's *Default home* / *Home screen* setting, or:

```text
adb shell cmd package set-home-activity --user 0 io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
```

Enable developer options and network debugging on the TV first (Settings → Device Preferences → About →
click *Build* 7 times). On Google TV the stock launcher may keep taking over even when the command prints
`Success`; [AGENTS.md](AGENTS.md) describes that case, along with the commands to restore it.

How the app works and where the data comes from: [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md).

## Credits

- Inspired by and started inside [Clean Your TV](https://cleanyour.tv/) by Gathin (MIT).
- Polish club fixtures: [kalendarzeligowe.pl](https://kalendarzeligowe.pl) — used with the site owner's permission.
- Foreign club fixtures (Top 5 leagues, domestic and European cups): match data by [fixtur.es](https://fixtur.es)
  — used under their fair-use rules; credited on every match card.
- Weather data: [Open-Meteo.com](https://open-meteo.com/) (CC BY 4.0); city search: Open-Meteo geocoding /
  GeoNames (CC BY 4.0).
- Weather icons: [Meteocons](https://github.com/basmilius/weather-icons) by Bas Milius (MIT), bundled in the app.
- Weather Icons font by Erik Flowers (SIL OFL 1.1) — used for the °C / °F sign.
- Club crests: [kalendarzeligowe.pl](https://kalendarzeligowe.pl); foreign clubs without a crest there:
  [football-logos.cc](https://football-logos.cc). Crests belong to their clubs — see [docs/CRESTS.md](docs/CRESTS.md).

## License

Code: [MIT](LICENSE). Club crests and third-party logos are not covered by the code license.
