# Tłumaczenia / Translations

## Polski

Wersja deweloperska obsługuje PL, EN, DE, ES i IT oraz opcję języka systemowego. Dla nierozpoznanego języka systemowego używany jest angielski. Jawny wybór pozostaje zapisany niezależnie od języka TV. Zmiana jest stosowana od razu do pulpitu i otwartego panelu ustawień.

Katalogi `app/src/main/assets/i18n/{de,es,it}.json` obejmują teksty interfejsu, nazwy krajów i etapów rozgrywek oraz komunikaty i stronę wysyłania zdjęć z telefonu. Nazwy klubów i oficjalne nazwy lig pozostają zgodne ze źródłami. Daty i format liczb używają wybranego języka; strefy czasowe meczów i pogody pozostają bez zmian. Zewnętrzne komunikaty systemu Android zależą od języka systemowego.

`scripts/check-translations.py` sprawdza kompletność katalogów i poprawność kodowania; `LanguageChoiceCheck` testuje wybór automatyczny i ręczny. Oba są uruchamiane przez `scripts/test-local.ps1`. Private nie jest objęty tą zmianą.

## English

The development version supports PL, EN, DE, ES and IT, plus the system language option. Unsupported system languages fall back to English. An explicit choice is persisted independently of the TV language. Changes apply immediately to the dashboard and the open settings panel.

The catalogues `app/src/main/assets/i18n/{de,es,it}.json` cover UI text, country names, competition stages and the phone photo-upload page and responses. Club names and official league names remain as provided by their sources. Dates and numbers use the selected locale; fixture and weather time zones are unchanged. Android system dialogs follow the system language.

`scripts/check-translations.py` verifies catalogue coverage and encoding. `LanguageChoiceCheck` verifies automatic and manual selection. Both run through `scripts/test-local.ps1`. Private is unchanged.
