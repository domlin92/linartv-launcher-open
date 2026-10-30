<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/brand/linartv-header-dark.svg">
    <img alt="LinarTV — launcher for Android TV" src="docs/brand/linartv-header-light.svg" width="720">
  </picture>
</p>

<p align="center">
  <a href="https://github.com/domlin92/linartv-launcher-open/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/domlin92/linartv-launcher-open"></a>
  <a href="LICENSE"><img alt="MIT license" src="https://img.shields.io/badge/license-MIT-blue"></a>
  <img alt="Android TV 8.0+" src="https://img.shields.io/badge/Android%20TV-8.0%2B-3DDC84">
</p>

<p align="center"><a href="#english">English</a> · <a href="#polski">Polski</a></p>

<table align="center">
  <tr>
    <td align="center"><img alt="LinarTV home screen: Cracovia and Arsenal / ekran główny LinarTV: Cracovia i Arsenal" src="docs/images/linartv-home-2-clubs.png" width="420"><br><sub>Polish club + Premier League · Klub polski + Premier League</sub></td>
    <td align="center"><img alt="LinarTV in German with Bayern Munich and Munich weather / LinarTV po niemiecku z Bayernem i pogodą w Monachium" src="docs/images/linartv-home-1-club.png" width="420"><br><sub>One club, German interface · Jeden klub, interfejs niemiecki</sub></td>
  </tr>
  <tr>
    <td align="center"><img alt="Club search by name with a microphone button / wyszukiwanie klubu po nazwie z przyciskiem mikrofonu" src="docs/images/linartv-club-search.png" width="420"><br><sub>Club search, also by voice · Wyszukiwanie klubu, także głosem</sub></td>
    <td align="center"><img alt="Weather details with a 7-day forecast / szczegóły pogody z prognozą na 7 dni" src="docs/images/linartv-weather-details.png" width="420"><br><sub>Weather details · Szczegóły pogody</sub></td>
  </tr>
  <tr>
    <td align="center" colspan="2"><img alt="Update available bar / pasek „Dostępna aktualizacja”" src="docs/images/linartv-update-bar.png" width="420"><br><sub>Updates inside the app · Aktualizacje w aplikacji</sub></td>
  </tr>
</table>

---

## English

**LinarTV** is a clean, fast home screen for Android TV built around football — from the Polish IV liga to the European top 5. It shows the clock, the weather for your city and the next matches of one or two clubs you follow — and keeps your favourite apps one click away.

LinarTV replaces your home screen. It does not uninstall the TV manufacturer's apps or remove their advertising and tracking services. If you also want to tidy up the TV's system apps, [CleanYourTV](https://github.com/Gathin23/cleanyourtv) is a separate project you can use alongside LinarTV.

### Features
- **Your clubs' fixtures** — pick one or two teams: Polish clubs from Ekstraklasa down to all 16 IV liga groups ([kalendarzeligowe.pl](https://kalendarzeligowe.pl)) and clubs from the **Premier League, La Liga, Bundesliga, Serie A and Ligue 1** ([fixtur.es](https://fixtur.es)) — league, domestic cups and European cups in one schedule, with club crests. Mix them freely.
- **Find your club by name — or just say it**: club search in the setup wizard, with a microphone button that uses the TV's speech recognition.
- **Match card at a glance** — competition and round, kick-off time in your time zone, "in 3 days", a green TODAY badge and a LIVE badge.
- **Weather for any city in the world** — current conditions, hourly and 7-day forecast from Open-Meteo, with Meteocons weather icons; city search by keyboard or voice.
- **Polish, English, German, Spanish or Italian interface** — follows the TV's system language or the one you choose; Celsius or Fahrenheit.
- **Updates inside the app** — when a new release is out, LinarTV shows an "Update available" bar; one click downloads it and opens the install window. Your settings stay.
- **Day and night modes**, app dock and full app drawer, quick access to TV inputs.
- **Wallpapers from your phone** — scan a QR code and upload photos over your home Wi-Fi (6-digit code, attempt limit).
- **Screensaver** with your photos after 5 minutes of inactivity.

### Install with Downloader (no computer needed)
**Downloader code: `1241090`** (or open `aftv.news/1241090`). It always installs the latest release.
1. On the TV, install **Downloader** by AFTVnews from the Play Store.
2. Open it and enter **1241090**.
3. Install the APK (allow *Install unknown apps* for Downloader when asked).
4. Open **LinarTV** from the apps row. To make it your home screen, see your TV's *Default home* / *Home screen* settings, or set it from a computer:
   ```text
   adb shell cmd package set-home-activity --user 0 io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
   ```
   As the home screen, LinarTV opens after the TV starts and when it wakes from standby. On devices with plain Android (e.g. projectors) it is also listed among normal apps.

   **Google TV (Chromecast with Google TV, Homatics and similar):**
   - If the APK is blocked as harmful (Play Protect), choose *More details → Install anyway*, or turn off Play Protect scanning for a moment and turn it back on after installing.
   - Google TV keeps opening its own launcher even when the command above prints `Success`. To use LinarTV as the home screen, disable the Google TV launcher (at your own risk; not yet tested by the maintainer):
     ```text
     adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
     ```
     Undo: `adb shell pm enable com.google.android.apps.tv.launcherx`. LinarTV 1.0.1 or newer is required.

From version 1.0.2 on, LinarTV offers new versions by itself (Downloader is only needed for the first install).

Or download `LinarTV.apk` directly from [Releases](https://github.com/domlin92/linartv-launcher-open/releases/latest).

### Install with an AI coding assistant (Codex, Claude Code)
Open this repository in Codex, Claude Code or a similar tool and ask, for example: *"Install LinarTV on my Android TV"* (give it the TV's IP address from the TV's network settings). The assistant follows [AGENTS.md](AGENTS.md): it checks the tools, builds or downloads the APK, connects with `adb`, installs the app and — only after asking you — sets it as the home screen, keeping the commands to restore the original launcher. Enable developer options and network debugging on the TV first (Settings → Device Preferences → About → click *Build* 7 times).

How it works and where the data comes from: [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md) (in Polish, with UML diagrams).

### Build from source
JDK 17 and the Android SDK are required (`JAVA_HOME`, `ANDROID_HOME`).
```text
./gradlew :app:assembleDebug
```
Signed releases: see [docs/RELEASING.md](docs/RELEASING.md). What changed in each version: [CHANGELOG.md](CHANGELOG.md).

### Credits
- Inspired by and started inside [Clean Your TV](https://cleanyour.tv/) by Gathin (MIT).
- Polish club fixtures: [kalendarzeligowe.pl](https://kalendarzeligowe.pl) — used with the site owner's permission.
- Foreign club fixtures (Top 5 leagues, domestic and European cups): match data by [fixtur.es](https://fixtur.es) — used under their fair-use rules; credited on every match card.
- Weather data: [Open-Meteo.com](https://open-meteo.com/) (CC BY 4.0); city search: Open-Meteo geocoding / GeoNames (CC BY 4.0).
- Weather icons: [Meteocons](https://github.com/basmilius/weather-icons) by Bas Milius (MIT), bundled in the app.
- Weather Icons font by Erik Flowers (SIL OFL 1.1) — used for the °C / °F sign.
- Club crests: [kalendarzeligowe.pl](https://kalendarzeligowe.pl); foreign clubs without a crest there: [football-logos.cc](https://football-logos.cc). Crests belong to their clubs — see [docs/CRESTS.md](docs/CRESTS.md).

### License
Code: [MIT](LICENSE). Club crests and third-party logos are not covered by the code license.

---

## Polski

**LinarTV** to czytelny, szybki ekran główny dla Android TV z myślą o piłce — od polskiej IV ligi po europejską Top 5. Pokazuje zegar, pogodę dla Twojego miasta i najbliższe mecze jednego lub dwóch klubów — a ulubione aplikacje są zawsze pod ręką.

LinarTV zastępuje ekran główny. Nie odinstalowuje aplikacji producenta telewizora ani nie usuwa jego usług reklamowych i śledzących. Jeśli chcesz również uporządkować aplikacje systemowe TV, [CleanYourTV](https://github.com/Gathin23/cleanyourtv) to osobny projekt, z którego możesz korzystać obok LinarTV.

### Funkcje
- **Terminarz Twoich klubów** — wybierz jedną lub dwie drużyny: polskie od Ekstraklasy po wszystkie 16 grup IV ligi ([kalendarzeligowe.pl](https://kalendarzeligowe.pl)) oraz kluby z **Premier League, La Ligi, Bundesligi, Serie A i Ligue 1** ([fixtur.es](https://fixtur.es)) — liga, krajowe i europejskie puchary w jednym terminarzu, z herbami klubów. Miksuj dowolnie.
- **Znajdź klub po nazwie — albo po prostu powiedz**: wyszukiwanie klubu w kreatorze z przyciskiem mikrofonu, który korzysta z rozpoznawania mowy telewizora.
- **Karta meczu w skrócie** — rozgrywki i kolejka, godzina w Twojej strefie czasowej, „za 3 dni”, zielony znaczek DZISIAJ i znaczek LIVE.
- **Pogoda dla dowolnego miasta na świecie** — aktualne warunki, prognoza godzinowa i 7-dniowa z Open-Meteo oraz ikony Meteocons; miasto wyszukasz klawiaturą albo głosem.
- **Język polski, angielski, niemiecki, hiszpański lub włoski** — jak w systemie telewizora albo wybrany przez Ciebie; temperatura w °C lub °F.
- **Aktualizacje w aplikacji** — gdy pojawi się nowa wersja, LinarTV pokaże pasek „Dostępna aktualizacja”; jedno kliknięcie pobiera ją i otwiera okno instalacji. Ustawienia zostają.
- **Tryb dzienny i nocny**, pasek aplikacji i pełna lista aplikacji, szybki dostęp do źródeł TV.
- **Tapety z telefonu** — zeskanuj kod QR i wyślij zdjęcia przez domowe Wi-Fi (6-cyfrowy kod z limitem prób).
- **Wygaszacz** z Twoimi zdjęciami po 5 minutach bezczynności.

### Instalacja z asystentem AI (Codex, Claude Code)
Otwórz to repozytorium w Codex, Claude Code lub podobnym narzędziu i poproś np.: *„Zainstaluj LinarTV na moim telewizorze”* (podaj adres IP telewizora z jego ustawień sieci). Asystent korzysta z [AGENTS.md](AGENTS.md): sprawdza narzędzia, buduje lub pobiera APK, łączy się przez `adb`, instaluje aplikację i — dopiero po Twojej zgodzie — ustawia ją jako ekran główny, zapisując polecenia przywracające fabryczny launcher. Wcześniej włącz na TV opcje programisty i debugowanie sieciowe (Ustawienia → Ustawienia urządzenia → Informacje → 7× *Kompilacja*).

Opis działania i źródeł danych: [docs/FUNCTIONAL.md](docs/FUNCTIONAL.md) (z diagramami UML).

### Instalacja przez Downloader (bez komputera)
**Kod do Downloadera: `1241090`** (albo adres `aftv.news/1241090`). Zawsze instaluje najnowsze wydanie.
1. Na telewizorze zainstaluj **Downloader** (AFTVnews) ze Sklepu Play.
2. Otwórz go i wpisz **1241090**.
3. Zainstaluj APK (zezwól Downloaderowi na *instalowanie nieznanych aplikacji*).
4. Uruchom **LinarTV** z listy aplikacji. Aby ustawić go jako ekran główny, poszukaj w ustawieniach TV opcji *Domyślny ekran główny* albo ustaw go z komputera:
   ```text
   adb shell cmd package set-home-activity --user 0 io.github.domlin92.linartv.open/com.linart.tvpreview.DashboardActivity
   ```
   Jako ekran główny LinarTV otwiera się po włączeniu telewizora i po wybudzeniu z czuwania. Na urządzeniach ze zwykłym Androidem (np. projektory) widać go też na liście zwykłych aplikacji.

   **Google TV (Chromecast z Google TV, Homatics i podobne):**
   - Jeśli instalacja jest blokowana jako szkodliwa (Play Protect), wybierz *Więcej szczegółów → Zainstaluj mimo to* albo na chwilę wyłącz skanowanie Play Protect i włącz je po instalacji.
   - Google TV otwiera własny launcher, nawet gdy powyższe polecenie zwróci `Success`. Aby LinarTV był ekranem głównym, wyłącz launcher Google TV (na własną odpowiedzialność; opiekun jeszcze tego nie testował):
     ```text
     adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
     ```
     Przywrócenie: `adb shell pm enable com.google.android.apps.tv.launcherx`. Wymagana wersja LinarTV 1.0.1 lub nowsza.

Od wersji 1.0.2 LinarTV sam proponuje nowe wersje (Downloader jest potrzebny tylko przy pierwszej instalacji).

Plik `LinarTV.apk` możesz też pobrać bezpośrednio z [Releases](https://github.com/domlin92/linartv-launcher-open/releases/latest).

### Budowanie
Wymagane: JDK 17 i Android SDK (`JAVA_HOME`, `ANDROID_HOME`).
```text
./gradlew :app:assembleDebug
```
Podpisane wydania: [docs/RELEASING.md](docs/RELEASING.md). Zmiany w kolejnych wersjach: [CHANGELOG.md](CHANGELOG.md). Plan prac: [docs/ROADMAP.md](docs/ROADMAP.md).

### Podziękowania
- Projekt powstał na bazie i z inspiracji [Clean Your TV](https://cleanyour.tv/) (Gathin, MIT).
- Terminarze polskich klubów: [kalendarzeligowe.pl](https://kalendarzeligowe.pl) — za zgodą właściciela serwisu.
- Terminarze klubów zagranicznych (Top 5 lig, krajowe i europejskie puchary): dane meczowe [fixtur.es](https://fixtur.es) — na zasadach „fair use” fixtur.es; podpis na każdej karcie meczu.
- Dane pogodowe: [Open-Meteo.com](https://open-meteo.com/) (CC BY 4.0); wyszukiwanie miast: geokodowanie Open-Meteo / GeoNames (CC BY 4.0).
- Ikony pogody: [Meteocons](https://github.com/basmilius/weather-icons), Bas Milius (MIT), wbudowane w aplikację.
- Font Weather Icons: Erik Flowers (SIL OFL 1.1) — znak °C / °F.
- Herby: [kalendarzeligowe.pl](https://kalendarzeligowe.pl); kluby zagraniczne bez herbu tam: [football-logos.cc](https://football-logos.cc). Herby należą do klubów — zobacz [docs/CRESTS.md](docs/CRESTS.md).

### Licencja
Kod: [MIT](LICENSE). Herby klubów i logo innych firm nie są objęte licencją kodu.
