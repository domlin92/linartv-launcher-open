package com.linart.tvpreview;

import java.util.Arrays;
import java.util.Locale;

/** Persist the choice separately from the resolved system language. */
final class LanguageChoice {
    static final String[] CODES = {"auto", "pl", "en", "de", "es", "it"};
    static String resolve(String choice, Locale system) {
        String code = "auto".equals(choice) || choice == null ? system.getLanguage() : choice;
        return Arrays.asList("pl", "en", "de", "es", "it").contains(code) ? code : "en";
    }
    private LanguageChoice() { }
}
