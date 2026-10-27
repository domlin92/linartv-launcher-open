# Testy lokalne / Local testing

## Polski

### Bez telewizora
Z JDK 17 w `JAVA_HOME`, w katalogu repozytorium:
```powershell
.\scripts\test-local.ps1
.\gradlew.bat :app:assembleDebug
```
Pierwsze polecenie sprawdza logikę bez Androida: harmonogram trybu dzień/noc (także przez północ), wymuszony wygląd, czas wygaszacza, rozmycie okien (piksel po pikselu względem poprzedniego algorytmu), rozpoznawanie rozgrywek, zakres kalendarzy i dopasowanie nazw herbów. Nie testuje widoków ani nawigacji pilotem. Drugie polecenie kompiluje APK.

### Emulator
Emulator Android TV (Android Studio) lub BlueStacks z włączonym ADB pozwala sprawdzić prawdziwe APK: fokus pilota, Wstecz, zapis ustawień, galerię tapet i okna. Użyj `adb connect 127.0.0.1:<port>` i `adb install -r <apk>`. Nie wyłączaj weryfikacji certyfikatów HTTPS przy pobieraniu obrazów systemu.

Najbliżej typowego telewizora jest emulator **Android TV 8.0 (API 26), 1920×1080** z Android Studio (obraz „Android TV Intel x86 Atom System Image”, profil urządzenia `tv_1080p`). Program antywirusowy, który sprawdza ruch HTTPS na komputerze (np. Norton Web/Mail Shield), podmienia certyfikaty stron; Android w emulatorze im nie ufa i aplikacja nie pobierze terminarzy, pogody ani herbów. Na czas testów wyłącz tę funkcję albo dodaj wyjątek dla emulatora.

Wejścia HDMI, Ambilight, Cast, ograniczenia procesów w tle, wybudzanie i wydajność trzeba sprawdzić na prawdziwym telewizorze.

### Lista kontrolna przed wydaniem
1. Pasek narzędzi: Oglądaj telewizję, Źródła, Ustawienia — w tej kolejności.
2. Ustawienia otwierają panel boczny; pilot dociera do wszystkich kafelków i przycisków; Wstecz zamyka najpierw podmenu, potem panel, a fokus wraca na pulpit.
3. RAM i pamięć widać tylko w panelu głównym.
4. Tapety: galeria i wysyłanie z telefonu działają.
5. Wygaszacz: Wyłączony, gotowe czasy i własny czas 1–1440 min; po restarcie ustawienie zostaje.
6. Wygląd: Automatycznie, Zawsze jasny, Zawsze ciemny, własne godziny (identyczne godziny są odrzucane; domyślnie 08:00 / 20:00).
7. Pogoda: wyszukiwanie miasta na świecie i °C/°F. Kalendarze: kraj → liga → klub (Polska i Top 5), jeden lub dwa kluby, „Odśwież teraz”; na karcie podpis „Dane dostarcza: …”.
8. Język polski i angielski.
9. Zmiana ustawień lub odświeżenie danych przy otwartym oknie nie podmienia pulpitu pod spodem; przebudowa następuje po zamknięciu okien.
10. Dock: przytrzymanie OK → Przesuń, Zamień, Usuń; kolejność zostaje po restarcie.

Zamykanie aplikacji w tle prosi Androida o zakończenie procesów; system może odmówić, więc interfejs nie obiecuje odzyskanej pamięci.

## English

### Without a TV
With JDK 17 on `JAVA_HOME`, from the repository root:
```powershell
.\scripts\test-local.ps1
.\gradlew.bat :app:assembleDebug
```
The first command checks logic without Android: day/night scheduling (including across midnight), forced appearance, screensaver timeout, popup blur (pixel by pixel against the previous algorithm), competition detection, calendar scope and crest name matching. It does not test views or remote navigation. The second command builds the APK.

### Emulator
An Android TV emulator (Android Studio) or BlueStacks with ADB enabled runs the real APK: remote focus, Back, settings persistence, wallpaper gallery and popups. Use `adb connect 127.0.0.1:<port>` and `adb install -r <apk>`. Do not disable HTTPS certificate validation to download system images.

Closest to a typical TV is the **Android TV 8.0 (API 26), 1920×1080** emulator from Android Studio ("Android TV Intel x86 Atom System Image", device profile `tv_1080p`). Antivirus software that inspects HTTPS on the computer (e.g. Norton Web/Mail Shield) replaces site certificates; Android in the emulator does not trust them, so fixtures, weather and crests will not load. Turn that feature off while testing or add an exception for the emulator.

HDMI inputs, Ambilight, Cast, background-process limits, wake-up and performance need a real television.

### Release checklist
1. Toolbar: Watch TV, Inputs, Settings — in that order.
2. Settings opens the side panel; the remote reaches every tile and button; Back closes a submenu first, then the panel, returning focus to home.
3. RAM and storage appear only in the main panel.
4. Wallpapers: gallery and phone upload work.
5. Screensaver: Off, presets and a custom 1–1440 min value; the setting survives a restart.
6. Appearance: Automatic, Always light, Always dark, custom hours (identical hours are rejected; defaults 08:00 / 20:00).
7. Weather: worldwide city search and °C/°F. Calendars: country → league → club (Poland and Top 5), one or two clubs, "Refresh now"; the card shows "Data provided by: …".
8. Polish and English interface.
9. Changing settings or finishing a data refresh while a popup is open does not replace the dashboard underneath; the rebuild happens after popups close.
10. Dock: hold OK → Move, Replace, Remove; the order survives a restart.

Closing background apps asks Android to stop processes; the system may refuse, so the UI never promises freed memory.
