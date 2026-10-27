package com.linart.tvpreview;

import android.content.Context;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Immutable per-language catalogue; English text is a stable fallback key. */
final class Translations {
    private final String code;
    private final JSONObject catalog;
    Translations(Context context, String choice) {
        code = LanguageChoice.resolve(choice, Locale.getDefault());
        JSONObject loaded = new JSONObject();
        if (!"pl".equals(code) && !"en".equals(code)) {
            try (InputStream in = context.getAssets().open("i18n/" + code + ".json");
                 ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) bytes.write(buffer, 0, count);
                loaded = new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            } catch (Exception failure) {
                android.util.Log.w("LinarTV", "Translation catalogue unavailable: " + code, failure);
            }
        }
        catalog = loaded;
    }
    String code() { return code; }
    Locale locale() { return Locale.forLanguageTag(code); }
    String tr(String polish, String english) {
        return "pl".equals(code) ? polish : catalog.optString(english, english);
    }
}
