package com.linart.tvpreview;

import java.util.Locale;

public final class LanguageChoiceCheck {
    public static void main(String[] args) {
        check("auto", "de-AT", "de");
        check("auto", "es-MX", "es");
        check("auto", "it-CH", "it");
        check("auto", "pl-PL", "pl");
        check("auto", "en-US", "en");
        check("auto", "pt-BR", "en");
        check("es", "pl-PL", "es");
        check("pl", "de-DE", "pl");
        check("invalid", "it-IT", "en");
        check(null, "it-IT", "it");
        if (LanguageChoice.CODES.length != 6) throw new AssertionError("Language menu mismatch");
        System.out.println("OK: 11 language selection checks");
    }
    private static void check(String choice, String system, String expected) {
        if (!expected.equals(LanguageChoice.resolve(choice, Locale.forLanguageTag(system))))
            throw new AssertionError(choice + ": " + system);
    }
}
