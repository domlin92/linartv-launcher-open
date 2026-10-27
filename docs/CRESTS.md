# Herby klubowe / Club crests

**PL.** Aplikacja nie zawiera żadnych plików herbów — ani w repozytorium, ani w APK. Herby są pobierane dopiero na telewizorze, raz na klub, i zapisywane w jego pamięci.

### Skąd pochodzą
1. **[kalendarzeligowe.pl](https://kalendarzeligowe.pl)** (`/api/teams/{id}/logo`) — źródło podstawowe, używane za zgodą właściciela serwisu. Herb jest pobierany ponownie tylko wtedy, gdy zmieni się jego wersja (`crestVer`).
2. **[football-logos.cc](https://football-logos.cc)** — dla klubów, których herbu nie ma w kalendarzeligowe.pl (głównie kluby zagraniczne z fixtur.es i ich rywale w pucharach). Herb jest dopasowywany po nazwie klubu (`CrestNameMatcher`, reguły i wyjątki w kodzie i testach). Zgodnie z prośbą strony podajemy ją jako źródło w README, w tym pliku i w oknie „O aplikacji”.
3. Bez herbu — inicjały klubu.

### Prawa i usuwanie
Herby są znakami poszczególnych klubów i **nie są objęte licencją MIT** tego repozytorium. Służą wyłącznie do rozpoznania drużyn w terminarzu. Jeśli reprezentujesz klub albo źródło herbów i chcesz, by herb przestał być wyświetlany, załóż zgłoszenie (issue) w repozytorium — usuniemy go w najbliższym wydaniu.

---

**EN.** The app contains no crest files, neither in the repository nor in the APK. Crests are downloaded on the TV only, once per club, and cached there.

### Sources
1. **[kalendarzeligowe.pl](https://kalendarzeligowe.pl)** (`/api/teams/{id}/logo`) — the main source, used with the site owner's permission; a crest is downloaded again only when its version (`crestVer`) changes.
2. **[football-logos.cc](https://football-logos.cc)** — for clubs without a crest at kalendarzeligowe.pl (mostly foreign clubs from fixtur.es and their cup opponents), matched by club name (`CrestNameMatcher`, rules and exceptions in the code and tests). As the site asks, it is credited in the README, in this file and in the app's About window.
3. No crest — the club's initials.

### Rights and removal
Crests are trademarks of their clubs and are **not covered by the MIT license**. They are shown only to identify teams in the schedule. If you represent a club or a crest source and want a crest removed, please open an issue — it will be removed in the next release.
