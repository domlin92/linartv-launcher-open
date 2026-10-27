# Zmiany / Changelog

Każde wydanie ma tu wpis po polsku i po angielsku. Najnowsze na górze.
Every release gets an entry here in Polish and English. Newest first.

## Niewydane / Unreleased

- Interfejs niemiecki, hiszpański i włoski (DE/ES/IT): ustawienia, kreator klubów, pogoda, etapy rozgrywek oraz strona przesyłania zdjęć. Automatyczny wybór rozpoznaje pięć języków, daty i liczby są lokalizowane, a zmiana języka działa natychmiast.
- German, Spanish and Italian interfaces (DE/ES/IT): settings, club setup, weather, competition stages and the phone upload page. Automatic selection recognises five languages; dates and numbers are localised and language changes apply immediately.

- Język aplikacji domyślnie jak w systemie TV: polski system → polski, każdy inny → angielski. W Ustawieniach → Język nowa opcja „Systemowy”; wcześniejszy ręczny wybór zostaje.
- The app language follows the TV system by default: Polish system → Polish, any other → English. Settings → Language has a new "System" option; an earlier manual choice is kept.
- Karty meczu i pogody mają na dole wąski pasek: z lewej „Dane dostarcza: **dostawca**”, z prawej czas odświeżenia. Karty są nieco wyższe.
- Match and weather cards have a thin bottom strip: "Data provided by: **provider**" on the left, the update time on the right. Cards are slightly taller.
- Tytuł meczu: gospodarz z lewej, gość z prawej, małe „vs.” pośrodku; długie nazwy zawijają się na dwie linie i zmniejszają, bez łamania słów. Inicjały w zastępczych herbach pomijają znaki typu „&”.
- Match title: host left, guest right, a small "vs." in the middle; long names wrap onto two lines and shrink without breaking words. Initials in placeholder crests skip characters such as "&".
- Herby klubów zagranicznych z [football-logos.cc](https://football-logos.cc), gdy kalendarzeligowe.pl nie ma herbu. Źródła danych i herbów są wymienione w oknie „O aplikacji”.
- Foreign club crests from [football-logos.cc](https://football-logos.cc) when kalendarzeligowe.pl has none. Data and crest sources are listed in the About window.
- Nazwy klubów w kolumnach (gospodarz z lewej, gość z prawej), gdy mecz nie mieści się w jednej linii; godziny meczów w strefie czasowej telewizora, także dla meczów bez ustalonej godziny.
- Club names in columns (host left, guest right) when the match does not fit on one line; kick-off times in the TV's time zone, also for date-only matches.
- **Ligi zagraniczne:** Anglia, Francja, Hiszpania, Niemcy i Włochy (Premier League, Ligue 1, La Liga, Bundesliga, Serie A). W kreatorze najpierw kraj (Polska, potem reszta alfabetycznie), potem liga i klub. Dane z fixtur.es: liga, krajowe puchary (np. EFL Cup, Copa del Rey) i europejskie puchary w jednym terminarzu.
- **Foreign leagues:** England, France, Germany, Italy and Spain (Premier League, Ligue 1, Bundesliga, Serie A, La Liga). The wizard asks for the country first (Poland, then the rest alphabetically), then the league and the club. Data from fixtur.es: league, domestic cups (e.g. EFL Cup, Copa del Rey) and European cups in one schedule.
- Data meczu w dwóch liniach: „Sobota, 7 listopada”, a pod nią „za 12 dni”. W dniu meczu zamiast daty świeci zielony znaczek DZISIAJ (jak LIVE).
- The match date has two lines: "Saturday, 7 November", then "in 12 days". On match day a green TODAY badge (like LIVE) replaces the date.
- Nagłówek karty meczu ma zawsze dwie linie: liga, a pod nią kolejka lub etap — długie nazwy (np. IV liga warmińsko-mazurska) nie ucinają już numeru kolejki.
- The match card header always has two lines: the competition, then the round or stage — long names (e.g. IV liga warmińsko-mazurska) no longer cut off the round.

## 1.0.1 — 2026-10-26

**Polski**

Nowe
- Pogoda dla **dowolnego miasta na świecie** (Open-Meteo) zamiast tylko polskich miast z IMGW. Godziny prognozy są w czasie lokalnym wybranego miasta.
- Nowe, kolorowe ikony pogody Meteocons, wbudowane w aplikację (działają bez internetu).
- **IV liga** — wszystkie 16 grup wojewódzkich. W kreatorze: liga → „IV liga” → województwo → klub.
- **Herby wszystkich klubów z kalendarzeligowe.pl**, zawsze aktualne. Nowy herb pobiera się sam, gdy serwis go zmieni. Drużyny bez herbu mają inicjały.
- LinarTV może być **domyślnym ekranem głównym**. Wtedy otwiera się po włączeniu telewizora i po wybudzeniu z czuwania, a nie np. telewizja na żywo.
- Ikona na liście aplikacji także na zwykłym Androidzie (np. projektory jak Wanbo T2 Max).

Ulepszone
- Większe ikony i temperatury na karcie pogody. Długie nazwy miast mieszczą się w jednej lub dwóch liniach, bez ucinania.
- Plik aplikacji jest dużo mniejszy (0,7 MB zamiast 6,2 MB), bo herby nie są już w nim zapisane.

Do wiadomości
- Po aktualizacji telewizor może zapytać, która aplikacja ma być ekranem głównym. Wybierz LinarTV i „Zawsze”.

**English**

New
- Weather for **any city in the world** (Open-Meteo) instead of Polish cities from IMGW only. Forecast hours use the chosen city's local time.
- New colourful Meteocons weather icons, bundled in the app (work offline).
- **IV liga** — all 16 regional groups. In the wizard: league → "IV liga" → region → club.
- **Crests of all clubs from kalendarzeligowe.pl**, always current. A new crest downloads by itself when the site changes it. Teams without a crest show initials.
- LinarTV can be the **default home screen**. It then opens after the TV is switched on and after waking from standby, instead of e.g. live TV.
- App icon in the app list on plain Android as well (e.g. projectors such as the Wanbo T2 Max).

Improved
- Larger icons and temperatures on the weather card. Long city names fit on one or two lines, never cut off.
- The app file is much smaller (0.7 MB instead of 6.2 MB) because crests are no longer stored in it.

Good to know
- After the update the TV may ask which app should be the home screen. Choose LinarTV and "Always".

## 1.0.0 — 2026-10-25

Pierwsze wydanie: terminarz jednego lub dwóch klubów (Ekstraklasa – III liga, Puchar Polski) z herbami, pogoda IMGW dla polskiego miasta, dock aplikacji, tryb dzienny i nocny, tapety z telefonu, wygaszacz, pasek Spotify, język polski lub angielski.

First release: fixtures of one or two clubs (Ekstraklasa – III liga, Polish Cup) with crests, IMGW weather for a Polish city, app dock, day and night modes, wallpapers from your phone, screensaver, Spotify bar, Polish or English.
