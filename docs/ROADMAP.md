# Plan / Roadmap

## Polski

### Materiały do README — na później
- [ ] Dodać więcej aktualnych zrzutów ekranu: tryb jasny/ciemny, panel ustawień, wybór klubów, szczegóły pogody, galeria tapet i pasek Spotify. Używać neutralnych tapet i danych demonstracyjnych.

Aktualny zakres Open: wyłącznie kalendarzeligowe.pl — Ekstraklasa, I liga, II liga, cztery grupy III ligi i 16 grup IV ligi. Pogoda z Open-Meteo dla dowolnego miasta, współrzędne miast z geokodowania Open-Meteo. Obsługa fixtur.es została usunięta z kodu do czasu otrzymania zgody operatora; notatki o niej poniżej dotyczą przyszłych funkcji.

### 1.0.0 — pierwsze wydanie
- [x] Wybór jednego lub dwóch klubów z katalogu kalendarzeligowe.pl (za zgodą właściciela serwisu).
- [x] Prognoza i aktualne warunki z Open-Meteo dla miast na całym świecie, ikony Meteocons (1.0.1; wcześniej IMGW tylko dla Polski).
- [x] Ekran główny: filtr HOME, ikona na zwykłym Androidzie, otwieranie po starcie i wybudzeniu TV (1.0.1).
- [x] Galeria tapet, wysyłanie zdjęć z telefonu (kod z limitem prób), wygaszacz.
- [x] Herby wszystkich klubów pobierane z kalendarzeligowe.pl z kontrolą wersji; bez herbów w APK (1.0.1).
- [x] IV liga — 16 grup wojewódzkich (1.0.1).
- [x] Kluby zagraniczne Top 5 z fixtur.es (za zgodą, z podpisem źródła) — liga, krajowe i europejskie puchary (niewydane).
- [x] Dock: wybór aplikacji, zamiana, przesuwanie pilotem i usuwanie (przytrzymanie OK na kafelku).
- [x] Podpisane APK i instalacja przez Downloader (wydanie 1.0.0, kod `1241090`).
- [ ] Test na kilku urządzeniach Android TV / Google TV.

### Dock — kolejne kroki
- [ ] Przytrzymanie OK w pełnej liście aplikacji: „Dodaj do docka / Usuń z docka”.
- [ ] Limit kafelków dopasowany do szerokości paska; przy pełnym pasku komunikat zamiast cichego pominięcia.
- [ ] Odinstalowane aplikacje znikają z docka automatycznie.
- [ ] Dla aplikacji bez banera — ikona z systemu.
- [ ] „Przywróć domyślny układ” w ustawieniach.

### Jakość terminarzy
- [ ] Zweryfikować znaczenie `isDateTbd` w API kalendarzeligowe.pl — dla części meczów niższych lig API i źródła regionalne podają tę samą godzinę mimo flagi `true`; obecnie LinarTV ukrywa godzinę.

### Mecze z całego świata (fixtur.es) — tylko po zgodzie operatora
Pomysł: **Polska → kalendarzeligowe.pl; świat → kalendarze ICS fixtur.es** (De Agendamakers B.V.). Właściciel projektu wysłał pytanie o warunki korzystania.
- [ ] Wybór kraj → liga → drużyna w kreatorze, bez pokazywania technicznego wyboru dostawcy.
- [ ] Parser ICS dla formatu fixtur.es (rozgrywki, kolejka, strefy czasowe, terminy bez potwierdzonej godziny).
- [ ] Opis wydarzenia z kalendarza w szczegółach meczu (bez ukrywania treści dostawcy) i atrybucja w README.
- [ ] Herby klubów zagranicznych: inicjały, bez nowych grafik bez zgody.
- [ ] Domyślnie angielski interfejs, gdy system TV nie jest po polsku.

### Pełne pokrycie polskich lig (do ustalenia)
- [ ] Zapytać właściciela kalendarzeligowe.pl o niższe klasy rozgrywkowe.
- [ ] laczynaspilka.pl (PZPN) wymaga tokenu po weryfikacji reCAPTCHA — nie obchodzimy tego; jedyna droga to oficjalna zgoda PZPN lub wojewódzkiego ZPN.
- [ ] Nie pobierać danych z serwisów, które zabraniają tego w `robots.txt`.

### Później
- Podział `DashboardActivity` na mniejsze klasy.

## English

### README media — later
- [ ] Add more current screenshots: light/dark appearance, settings panel, club selection, weather details, wallpaper gallery and Spotify bar. Use neutral wallpapers and demo data.

Current Open scope: kalendarzeligowe.pl only — Ekstraklasa, I liga, II liga, the four III liga groups and the 16 IV liga groups. Weather from Open-Meteo for any city, city coordinates from Open-Meteo geocoding. fixtur.es support was removed from the code until the operator agrees; the notes about it below describe future features.

### 1.0.0 — first release
- [x] One or two clubs from the kalendarzeligowe.pl catalogue (with the site owner's permission).
- [x] Open-Meteo forecast and current conditions for cities worldwide, Meteocons icons (1.0.1; previously IMGW, Poland only).
- [x] Home screen: HOME filter, icon on plain Android, opens after TV start-up and wake (1.0.1).
- [x] Wallpaper gallery, phone photo upload (code with attempt limit), screensaver.
- [x] All crests fetched from kalendarzeligowe.pl with version checks; no crests bundled in the APK (1.0.1).
- [x] IV liga — all 16 regional groups (1.0.1).
- [x] Top 5 foreign clubs from fixtur.es (with permission and credit) — league, domestic and European cups (unreleased).

- [x] Dock: choose apps, replace, move with the remote and remove (hold OK on a tile).
- [x] Signed APK and Downloader installation (release 1.0.0, code `1241090`).
- [ ] Test on several Android TV / Google TV devices.

### Dock — next steps
- [ ] Hold OK in the full app list: "Add to dock / Remove from dock".
- [ ] Tile limit matching the bar width; a message instead of silently skipping apps.
- [ ] Uninstalled apps disappear from the dock automatically.
- [ ] System icon for apps without a banner.
- [ ] "Restore default layout" in settings.

### Fixture quality
- [ ] Verify the meaning of `isDateTbd` in the kalendarzeligowe.pl API — for some lower-league matches the API and regional sources give the same kick-off time despite the flag being `true`; LinarTV currently hides the time.

### Matches worldwide (fixtur.es) — only with the operator's consent
Idea: **Poland → kalendarzeligowe.pl; rest of the world → fixtur.es ICS calendars** (De Agendamakers B.V.). The maintainer has asked about the terms of use.
- [ ] Country → league → team in the wizard, without exposing the provider choice.
- [ ] ICS parser for the fixtur.es format (competition, round, time zones, dates without confirmed kick-off).
- [ ] Event description from the calendar in match details (provider content not hidden) and attribution in the README.
- [ ] Foreign club crests: initials, no new artwork without consent.
- [ ] English interface by default when the TV system language is not Polish.

### Full coverage of Polish leagues (to be agreed)
- [ ] Ask the kalendarzeligowe.pl owner about lower divisions.
- [ ] laczynaspilka.pl (PZPN) requires a token issued after reCAPTCHA — we do not bypass it; the only route is official consent from PZPN or a regional FA.
- [ ] Do not fetch data from services that forbid it in `robots.txt`.

### Later
- Split `DashboardActivity` into smaller classes.
