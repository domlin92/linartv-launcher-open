# Tapeta śledzi pogodę / Weather-following wallpaper

[Polski](#polski) · [English](#english)

---

## Polski

### Co to jest
Opcja **„Tapeta śledzi pogodę”** w galerii tapet (Ustawienia → Wygląd launchera → Tapety) sprawia, że tło pulpitu
pokazuje aktualną pogodę dla wybranego miasta: **7 rodzajów pogody × wariant dzienny i nocny = 14 tapet**.
Tapeta zmienia się sama, gdy zmienią się warunki (prognoza odświeża się co 30 minut), a między wariantem dziennym
i nocnym przełącza się razem z resztą interfejsu (patrz niżej).

| # | Rodzaj | Kody WMO (Open-Meteo) | Pliki |
|---|---|---|---|
| 1 | Bezchmurnie / słonecznie | 0, 1 | `weather_clear_day`, `weather_clear_night` |
| 2 | Częściowe zachmurzenie | 2 | `weather_partly_cloudy_day`, `weather_partly_cloudy_night` |
| 3 | Chmury / zachmurzenie całkowite | 3 i każdy nieznany kod | `weather_cloudy_day`, `weather_cloudy_night` |
| 4 | Deszcz | 51–57, 61–67, 80–82 (mżawka, deszcz, opady przelotne) | `weather_rain_day`, `weather_rain_night` |
| 5 | Śnieg | 71–77, 85–86 (także deszcz ze śniegiem) | `weather_snow_day`, `weather_snow_night` |
| 6 | Burza | 95, 96, 99 (w tym grad) | `weather_storm_day`, `weather_storm_night` |
| 7 | Mgła / zamglenie | 45, 48 | `weather_fog_day`, `weather_fog_night` |

Dzień i noc: decyduje tryb **Wyglądu launchera** — Automatycznie (dzień 08:00–19:59, noc 20:00–07:59), Zawsze jasny,
Zawsze ciemny albo Własne godziny. Tapeta przełącza się w tej samej chwili co kolory interfejsu, więc w trybie
„Zawsze jasny” zawsze widać wariant dzienny. Gdy nie ma jeszcze żadnej prognozy (pierwsze uruchomienie, brak
internetu), launcher pokazuje tapetę wybraną w galerii.

### Jak podmienić wygląd (tylko w kodzie źródłowym)
Tapety to **14 plików w `app/src/main/res/drawable/`**. Launcher nie pozwala ich edytować ani podmieniać z poziomu
telewizora — zmiany wprowadza się w repozytorium.

1. Otwórz plik, np. `app/src/main/res/drawable/weather_rain_night.xml`. Szablon to prosty gradient:
   ```xml
   <shape android:shape="rectangle">
       <gradient android:angle="270" android:startColor="#04090F"
           android:centerColor="#0B1620" android:endColor="#152836" />
   </shape>
   ```
   Zmień kolory albo rozbuduj plik (np. `layer-list` z kształtami, chmurami, słońcem, księżycem).
2. Możesz też wstawić własną grafikę: usuń `weather_rain_night.xml` i połóż w tym samym miejscu
   `weather_rain_night.png`, `.webp` lub `.jpg` (1920×1080). **Nazwa pliku musi zostać ta sama** — kod nie wymaga
   wtedy żadnych zmian.
3. Zbuduj APK jak zwykle (`.\gradlew.bat :app:assembleDebug`). Wszystko działa bez internetu, pliki są w APK.

Zasady, o których warto pamiętać: tapeta leży pod półprzezroczystą zasłoną i kartami pulpitu, więc kontrast
napisów (zegar, karty) zależy od tła — bezpiecznie jest trzymać górną i lewą część obrazu możliwie jednolitą i
niezbyt jasną w wariancie dziennym. Kafelki i dock są rysowane na wierzchu.

### Kod
- `app/src/main/java/com/linart/tvpreview/WeatherWallpaper.java` — mapowanie kodów WMO na 7 rodzajów i odczyt
  znacznika dnia/nocy. Bez zależności od Androida, sprawdzane przez `tests/WeatherWallpaperCheck.java`.
- `DashboardActivity` — `followsWeather()`, `weatherWallpaper()`, `weatherWallpaperDrawable()`
  (14 identyfikatorów zasobów) oraz wiersz przełącznika w `showWallpaperGallery()`.
- Ustawienie: `wallpaper_follows_weather` (boolean) w `SharedPreferences`; wybór zdjęcia w galerii wyłącza opcję.

---

## English

### What it is
**"Wallpaper follows the weather"** in the wallpaper gallery (Settings → Launcher appearance → Wallpapers) makes
the home screen background show the current weather for the chosen city:
**7 weather kinds × day and night = 14 wallpapers**. The background changes on its own when the conditions change
(the forecast refreshes every 30 minutes) and flips between the day and night version together with the rest of the
interface (see below).

| # | Kind | WMO codes (Open-Meteo) | Files |
|---|---|---|---|
| 1 | Clear / sunny | 0, 1 | `weather_clear_day`, `weather_clear_night` |
| 2 | Partly cloudy | 2 | `weather_partly_cloudy_day`, `weather_partly_cloudy_night` |
| 3 | Clouds / overcast | 3 and every unknown code | `weather_cloudy_day`, `weather_cloudy_night` |
| 4 | Rain | 51–57, 61–67, 80–82 (drizzle, rain, showers) | `weather_rain_day`, `weather_rain_night` |
| 5 | Snow | 71–77, 85–86 (sleet included) | `weather_snow_day`, `weather_snow_night` |
| 6 | Thunderstorm | 95, 96, 99 (hail included) | `weather_storm_day`, `weather_storm_night` |
| 7 | Fog / mist | 45, 48 | `weather_fog_day`, `weather_fog_night` |

Day and night follow **Launcher appearance** — Automatic (day 08:00–19:59, night 20:00–07:59), Always light,
Always dark or Custom hours. The background flips at the same moment as the interface colours, so with "Always
light" you always get the day variant. When there is no forecast yet (first run, no network), the launcher shows
the wallpaper chosen in the gallery.

### How to change the look (source code only)
The wallpapers are **14 files in `app/src/main/res/drawable/`**. The launcher does not let you edit or replace them
from the TV — the changes live in the repository.

1. Open a file, e.g. `app/src/main/res/drawable/weather_rain_night.xml`. The template is a plain gradient:
   ```xml
   <shape android:shape="rectangle">
       <gradient android:angle="270" android:startColor="#04090F"
           android:centerColor="#0B1620" android:endColor="#152836" />
   </shape>
   ```
   Change the colours or extend the file (e.g. a `layer-list` with shapes, clouds, sun or moon).
2. You can also drop in your own artwork: delete `weather_rain_night.xml` and put `weather_rain_night.png`,
   `.webp` or `.jpg` (1920×1080) in the same folder. **The file name must stay the same** — no code changes
   needed.
3. Build the APK as usual (`.\gradlew.bat :app:assembleDebug`). Everything works offline; the files are inside the
   APK.

Worth remembering: the wallpaper sits under a translucent veil and the dashboard cards, so the contrast of the
clock and cards depends on the background — keep the top and left of the artwork fairly even, and not too bright
in the day variant. The tiles and the dock are drawn on top.

### Code
- `app/src/main/java/com/linart/tvpreview/WeatherWallpaper.java` — WMO codes to the seven kinds and the day/night
  marker. No Android dependencies, covered by `tests/WeatherWallpaperCheck.java`.
- `DashboardActivity` — `followsWeather()`, `weatherWallpaper()`, `weatherWallpaperDrawable()`
  (the 14 resource ids) and the switch row in `showWallpaperGallery()`.
- Setting: `wallpaper_follows_weather` (boolean) in `SharedPreferences`; picking a photo in the gallery turns the
  option off.
