package com.linart.tvpreview;

import java.text.Normalizer;
import java.util.Locale;

/** Club names compared without case, Polish letters, accents or punctuation ("Śląsk-Wrocław" = "slask wroclaw"). */
final class ClubNames {
    private ClubNames() { }

    static String normalize(String name) {
        if (name == null) return "";
        return Normalizer.normalize(name.toLowerCase(Locale.ROOT).replace('\u0142', 'l'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replaceAll("[^a-z0-9]+", " ").trim();
    }
}
