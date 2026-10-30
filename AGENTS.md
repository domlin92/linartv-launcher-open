# Instructions for AI coding assistants / Instrukcje dla asystentów AI

This file is read by Codex, Claude Code and similar tools (Claude Code loads it through `CLAUDE.md`).
Plik czytają Codex, Claude Code i podobne narzędzia (Claude Code wczytuje go przez `CLAUDE.md`).

---

## English

### What this project is
LinarTV Open is an Android TV home screen (launcher) for football fans: clock, Open-Meteo weather for any city worldwide, fixtures of one or two clubs (Polish clubs from kalendarzeligowe.pl, top 5 foreign leagues from fixtur.es), club search by name or voice, five interface languages, in-app updates from GitHub Releases, an app dock, wallpapers and a screensaver. One activity (`DashboardActivity`), plain Java, no Kotlin, no Compose. Read [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md) first.

### Typical user request: "install LinarTV on my TV"
Work step by step and tell the user what each step does. Ask before every step that changes the TV.

1. **Prerequisites on the computer:** JDK 17 (`JAVA_HOME`), Android SDK platform 34 + build-tools (`ANDROID_HOME`), `adb` from platform-tools.
   Easiest alternative: download the signed `LinarTV.apk` from GitHub Releases instead of building.
2. **Build (optional):**
   ```bash
   ./gradlew :app:assembleDebug        # Windows: .\gradlew.bat :app:assembleDebug
   ```
   Output: `app/build/outputs/apk/debug/LinarTV-<version>-debug.apk`.
3. **Connect to the TV** (the user does this on the TV): Settings → Device Preferences → About → click *Build* 7×, then Developer options → enable *USB debugging* / *Network debugging*. Find the TV's IP address in network settings.
   ```bash
   adb connect <TV_IP>:5555            # accept the prompt on the TV
   adb install -r LinarTV.apk
   ```
4. **Open it:** `adb shell am start -n io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity`
5. **Make it the home screen (optional, ask first).** Prefer the TV's own setting (*Home screen* / *Default home app*) if it exists. Otherwise:
   ```bash
   adb shell cmd package set-home-activity --user 0 io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
   ```
   Many devices keep returning to the stock launcher even after `set-home-activity` prints `Success`. Disabling the stock launcher is possible, but **only after the user agrees** and after you have written down the restore commands. First find out which launcher the device uses:
   ```bash
   adb shell cmd package resolve-activity -c android.intent.category.HOME
   ```
   **Android TV** (e.g. Philips Android TV, many boxes):
   ```bash
   adb shell pm disable-user --user 0 com.google.android.tvlauncher
   # restore:
   adb shell pm enable com.google.android.tvlauncher
   adb shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
   ```
   **Google TV** (e.g. Chromecast with Google TV, Homatics, newer TVs) uses a different launcher package, so the command above does nothing there:
   ```bash
   adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
   # restore:
   adb shell pm enable com.google.android.apps.tv.launcherx
   ```
   The Google TV steps have not been tested by the maintainer yet; tell the user so. LinarTV 1.0.1 or newer is required (1.0.0 cannot be a home screen).
   **Play Protect:** on Google TV the APK may be blocked as "harmful" because it does not come from the Play Store. The user can choose *More details → Install anyway*, or temporarily turn off Play Protect scanning and turn it back on afterwards. `adb install` is usually not blocked.
   Never run `pm uninstall` on system packages and never disable packages you have not identified with `adb shell cmd package resolve-activity -c android.intent.category.HOME`.
6. **First run:** the wizard asks for one or two clubs and the dock apps. Weather city is in Settings → Weather. Spotify "now playing" needs notification access (Settings → Spotify), see [docs/SPOTIFY.md](docs/SPOTIFY.md).

### Rules when changing the code
- Run `scripts/test-local.ps1` (or the Java checks in `tests/`) and `./gradlew :app:assembleDebug` before proposing a change.
- **Versioning:** every published build bumps `versionCode` by 1 and sets a semantic `versionName` in `app/build.gradle`; tag releases `vX.Y.Z`. See [docs/RELEASING.md](docs/RELEASING.md).
- **Documentation is bilingual** (Polish + English). Update both languages.
- **Data sources:** only kalendarzeligowe.pl (used with the owner's permission), fixtur.es iCal feeds (Top 5 foreign clubs; fair use: credit "Data provided by: fixtur.es" on the card, each feed at most every 6 h with User-Agent and If-Modified-Since, no resale; the club list is bundled via scripts/update-fixtures-teams.py) and Open-Meteo forecast + geocoding (CC BY 4.0, attribution "Open-Meteo.com" shown in the weather details). Weather icons are the bundled Meteocons (MIT, `assets/meteocons`). Do not add new sources without the maintainer's consent; never bypass reCAPTCHA or ignore `robots.txt`.
- **Club crests** are third-party marks, not MIT. The app loads them from kalendarzeligowe.pl (`/api/teams/{id}/logo`); for clubs without a crest there (mostly foreign clubs) from [football-logos.cc](https://football-logos.cc), credited in README, CRESTS.md and the app's About window, as the site asks. Do not add crest files to the repository. See [docs/CRESTS.md](docs/CRESTS.md).
- **No secrets:** never commit keystores, passwords, IP addresses of the user's network or personal photos. Run `python scripts/audit_public.py` before every push.
- Keep the remote-control (D-pad) navigation working: every new view must be focusable and reachable with arrows; Back closes popups first.

---

## Polski

### Czym jest projekt
LinarTV Open to ekran główny (launcher) dla Android TV dla kibiców piłki: zegar, pogoda Open-Meteo dla dowolnego miasta na świecie, terminarz jednego lub dwóch klubów (polskie z kalendarzeligowe.pl, Top 5 lig zagranicznych z fixtur.es), wyszukiwanie klubu po nazwie lub głosem, pięć języków, aktualizacje z GitHub Releases, dock aplikacji, tapety i wygaszacz. Jedna aktywność (`DashboardActivity`), czysta Java, bez Kotlina i Compose. Najpierw przeczytaj [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md).

### Typowa prośba: „zainstaluj LinarTV na moim telewizorze”
Pracuj krok po kroku i mów użytkownikowi, co robi każdy krok. Pytaj przed każdym krokiem, który zmienia coś na telewizorze.

1. **Wymagania na komputerze:** JDK 17 (`JAVA_HOME`), Android SDK z platformą 34 i build-tools (`ANDROID_HOME`), `adb` z platform-tools.
   Najprościej: zamiast budować, pobierz podpisany `LinarTV.apk` z GitHub Releases.
2. **Budowanie (opcjonalnie):** `./gradlew :app:assembleDebug` (Windows: `.\gradlew.bat :app:assembleDebug`). Plik: `app/build/outputs/apk/debug/LinarTV-<wersja>-debug.apk`.
3. **Połączenie z TV** (użytkownik robi to na telewizorze): Ustawienia → Ustawienia urządzenia → Informacje → 7× kliknij *Kompilacja*, potem w Opcjach programisty włącz *Debugowanie USB* / *sieciowe*. Adres IP znajdziesz w ustawieniach sieci.
   ```bash
   adb connect <IP_TV>:5555            # potwierdź na telewizorze
   adb install -r LinarTV.apk
   ```
4. **Uruchomienie:** `adb shell am start -n io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity`
5. **Ustawienie jako ekran główny (opcjonalnie, zapytaj najpierw).** Najlepiej przez ustawienie telewizora (*Ekran główny* / *Domyślna aplikacja ekranu głównego*), jeśli istnieje. W przeciwnym razie użyj `cmd package set-home-activity` (jak w części angielskiej). Wiele urządzeń mimo `Success` wraca do fabrycznego launchera. Wyłączenie go (`pm disable-user`) tylko za zgodą użytkownika i po zapisaniu poleceń przywracających. Najpierw sprawdź launcher poleceniem `resolve-activity` (jak w części angielskiej):
   - **Android TV:** `com.google.android.tvlauncher`,
   - **Google TV** (Chromecast z Google TV, Homatics, nowsze TV): `com.google.android.apps.tv.launcherx`; przywrócenie: `adb shell pm enable com.google.android.apps.tv.launcherx`. Kroków dla Google TV opiekun jeszcze nie testował — powiedz o tym użytkownikowi. Wymagana wersja LinarTV 1.0.1 lub nowsza.
   - **Play Protect:** na Google TV APK spoza Sklepu Play może zostać zablokowany jako „szkodliwy”. Użytkownik może wybrać *Więcej szczegółów → Zainstaluj mimo to* albo na chwilę wyłączyć skanowanie Play Protect i potem je włączyć. `adb install` zwykle nie jest blokowany.

   Nigdy nie używaj `pm uninstall` na pakietach systemowych.
6. **Pierwsze uruchomienie:** kreator pyta o kluby i aplikacje w docku. Miasto pogody: Ustawienia → Pogoda. Spotify wymaga dostępu do powiadomień ([docs/SPOTIFY.md](docs/SPOTIFY.md)).

### Zasady przy zmianach w kodzie
- Przed propozycją zmiany uruchom `scripts/test-local.ps1` i `./gradlew :app:assembleDebug`.
- **Wersjonowanie:** każde publikowane wydanie zwiększa `versionCode` o 1 i ustawia `versionName` (semver) w `app/build.gradle`; tag `vX.Y.Z`. Zob. [docs/RELEASING.md](docs/RELEASING.md).
- **Dokumentacja jest dwujęzyczna** (polski + angielski) — aktualizuj obie wersje.
- **Źródła danych:** tylko kalendarzeligowe.pl (za zgodą właściciela), kanały iCal fixtur.es (kluby zagraniczne Top 5; fair use: podpis „Dane dostarcza: fixtur.es” na karcie, każdy kanał najwyżej co 6 h z User-Agent i If-Modified-Since, bez odsprzedaży; lista klubów w aplikacji z scripts/update-fixtures-teams.py) oraz prognoza i geokodowanie Open-Meteo (CC BY 4.0, podpis „Open-Meteo.com” w szczegółach pogody). Ikony pogody to wbudowane Meteocons (MIT, `assets/meteocons`). Bez zgody opiekuna nie dodawaj nowych źródeł; nigdy nie obchodź reCAPTCHA ani `robots.txt`.
- **Herby** to znaki klubów, nie MIT. Aplikacja pobiera je z kalendarzeligowe.pl (`/api/teams/{id}/logo`); dla klubów bez herbu tam (głównie zagranicznych) z [football-logos.cc](https://football-logos.cc), z podaniem źródła w README, CRESTS.md i oknie „O aplikacji”, jak prosi strona; nie dodawaj plików herbów do repozytorium. Zob. [docs/CRESTS.md](docs/CRESTS.md).
- **Bez sekretów:** nie commituj kluczy, haseł, adresów sieci użytkownika ani zdjęć. Przed każdym pushem uruchom `python scripts/audit_public.py`.
- Zachowaj sterowanie pilotem: każdy nowy element musi mieć fokus i być osiągalny strzałkami; Wstecz najpierw zamyka okna.
