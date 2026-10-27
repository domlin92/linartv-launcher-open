package com.linart.tvpreview;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Top 5 foreign leagues from fixtur.es public iCal feeds, used under their fair-use rules
 * (2026-10-27): visible "Data provided by: fixtur.es" next to the fixture, each feed fetched at
 * most every 6 hours with a clear User-Agent and If-Modified-Since, no resale, no calendar sync.
 * One feed per club contains all its competitions (league, domestic cups, European cups).
 * The club list per league is bundled in assets/fixtures-teams.json
 * (scripts/update-fixtures-teams.py, once per season); the app reads only the iCal feeds.
 */
final class FixturesClient {
    static final class Country {
        final String code, namePl, nameEn, league, logoPage;
        Country(String code, String namePl, String nameEn, String league, String logoPage) {
            this.code = code; this.namePl = namePl; this.nameEn = nameEn; this.league = league;
            this.logoPage = logoPage;
        }
    }
    static final class Team {
        final String name, slug;
        Team(String name, String slug) { this.name = name; this.slug = slug; }
    }
    static final List<Country> COUNTRIES = Arrays.asList(
            new Country("en.1", "Anglia", "England", "Premier League", "england"),
            new Country("fr.1", "Francja", "France", "Ligue 1", "france"),
            new Country("es.1", "Hiszpania", "Spain", "La Liga", "spain"),
            new Country("de.1", "Niemcy", "Germany", "Bundesliga", "germany"),
            new Country("it.1", "Włochy", "Italy", "Serie A", "italy"));
    static final String FEED_PREFIX = "https://ics.fixtur.es/v2/";
    static final String PROVIDER = "fixtur.es";
    private static final long MIN_FETCH_INTERVAL_MS = 6L * 60 * 60 * 1000;
    private static final Pattern SUMMARY = Pattern.compile(
            "^(.+?) - (.+?)(?: \\[([^\\]]+)\\])?(?: \\(\\d+-\\d+\\))?$");
    /** fixtur.es competition tags; European ones use the Polish names translated by the UI. */
    private static final Map<String, String> TAGS = new HashMap<>();
    static {
        TAGS.put("CL", "Liga Mistrzów");
        TAGS.put("EL", "Liga Europy");
        TAGS.put("ECL", "Liga Konferencji");
        TAGS.put("UECL", "Liga Konferencji");
        TAGS.put("SuperCup", "Superpuchar UEFA");
        TAGS.put("FA", "FA Cup");
        TAGS.put("LC", "EFL Cup");
        TAGS.put("FA-CS", "Community Shield");
        TAGS.put("Copa", "Copa del Rey");
        TAGS.put("CS", "Supercopa de España");
        TAGS.put("DFB", "DFB-Pokal");
        TAGS.put("COP", "Coppa Italia");
        TAGS.put("CdF", "Coupe de France");
        TAGS.put("CdlL", "Coupe de la Ligue");
    }

    private static Context app;
    private static Map<String, List<Team>> teams;

    static void init(Context context) { app = context.getApplicationContext(); }

    static Country country(String code) {
        for (Country c : COUNTRIES) if (c.code.equals(code)) return c;
        return null;
    }

    /** Countries sorted by their name in the interface language. */
    static List<Country> sortedCountries(boolean english) {
        List<Country> list = new ArrayList<>(COUNTRIES);
        Collator collator = Collator.getInstance(english ? Locale.ENGLISH : new Locale("pl", "PL"));
        list.sort((a, b) -> collator.compare(english ? a.nameEn : a.namePl, english ? b.nameEn : b.namePl));
        return list;
    }

    static synchronized List<Team> teams(String code) throws Exception {
        if (teams == null) {
            Map<String, List<Team>> loaded = new HashMap<>();
            try (InputStream in = app.getAssets().open("fixtures-teams.json")) {
                JSONObject root = new JSONObject(new String(readAll(in, 500_000), StandardCharsets.UTF_8));
                for (Country c : COUNTRIES) {
                    JSONArray rows = root.optJSONArray(c.code);
                    List<Team> list = new ArrayList<>();
                    if (rows != null) for (int i = 0; i < rows.length(); i++)
                        list.add(new Team(rows.getJSONObject(i).getString("name"), rows.getJSONObject(i).getString("slug")));
                    loaded.put(c.code, list);
                }
            }
            teams = loaded;
        }
        List<Team> list = teams.get(code);
        return list == null ? new ArrayList<>() : list;
    }

    static String feedUrl(String slug) { return FEED_PREFIX + slug + ".ics"; }

    /** The club's upcoming matches; {@code leagueName} labels untagged (league) fixtures. */
    static List<IcsMatchParser.Match> teamMatches(String feedUrl, String leagueName, long fromMillis) throws Exception {
        String ics = feed(feedUrl);
        String unfolded = ics.replace("\r\n", "\n").replace('\r', '\n').replaceAll("\n[ \t]", "");
        List<IcsMatchParser.Match> result = new ArrayList<>();
        Map<String, String> event = null;
        for (String line : unfolded.split("\n")) {
            if (line.equals("BEGIN:VEVENT")) { event = new HashMap<>(); continue; }
            if (line.equals("END:VEVENT")) {
                if (event != null) addEvent(result, event, leagueName, fromMillis);
                event = null; continue;
            }
            if (event == null) continue;
            int colon = line.indexOf(':');
            if (colon < 0) continue;
            String key = line.substring(0, colon);
            if (key.startsWith("DTSTART")) event.put("DTSTART_KEY", key);
            event.put(key.split(";", 2)[0], line.substring(colon + 1));
        }
        result.sort((a, b) -> Long.compare(a.startsAt, b.startsAt));
        return result;
    }

    private static void addEvent(List<IcsMatchParser.Match> out, Map<String, String> event,
                                 String leagueName, long fromMillis) {
        try {
            if ("CANCELLED".equalsIgnoreCase(event.get("STATUS"))) return;
            Matcher summary = SUMMARY.matcher(unescape(event.getOrDefault("SUMMARY", "")).trim());
            if (!summary.matches()) return;
            String value = event.get("DTSTART");
            if (value == null) return;
            boolean hasTime = value.contains("T") && !event.getOrDefault("DTSTART_KEY", "").contains("VALUE=DATE");
            long startsAt;
            if (hasTime) {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US);
                format.setTimeZone(TimeZone.getTimeZone(value.endsWith("Z") ? "UTC" : "Europe/Warsaw"));
                startsAt = format.parse(value.endsWith("Z") ? value.substring(0, value.length() - 1) : value).getTime();
            } else {
                // Date only: midday UTC keeps the same date in any European/American time zone.
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                startsAt = format.parse(value.substring(0, 8)).getTime() + 12L * 60 * 60 * 1000;
            }
            if (startsAt < fromMillis) return;
            String tag = summary.group(3);
            String competition = tag == null ? leagueName : TAGS.getOrDefault(tag, tag);
            out.add(new IcsMatchParser.Match(summary.group(1).trim(), summary.group(2).trim(), startsAt,
                    hasTime, -1, competition, ""));
        } catch (Exception ignored) { }
    }

    /**
     * Cached feed: fetched at most every 6 hours, with If-Modified-Since. The body (often
     * 200+ kB) lives in a cache file; only the timestamps sit in SharedPreferences.
     */
    private static String feed(String feedUrl) throws Exception {
        SharedPreferences prefs = app.getSharedPreferences("fixtures_feeds", Context.MODE_PRIVATE);
        String key = Integer.toHexString(feedUrl.hashCode());
        java.io.File file = new java.io.File(app.getCacheDir(), "fixtures-" + key + ".ics");
        String cached = file.isFile()
                ? new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
                : prefs.getString("body_" + key, null); // feeds cached by builds before the file cache
        long fetchedAt = prefs.getLong("at_" + key, 0), now = System.currentTimeMillis();
        if (cached != null && now - fetchedAt < MIN_FETCH_INTERVAL_MS) return cached;
        HttpURLConnection connection = (HttpURLConnection) new URL(feedUrl).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("User-Agent", userAgent());
        String lastModified = prefs.getString("lm_" + key, null);
        if (cached != null && lastModified != null) connection.setRequestProperty("If-Modified-Since", lastModified);
        try {
            int code;
            try { code = connection.getResponseCode(); }
            catch (Exception offline) { if (cached != null) return cached; throw offline; } // keep the last feed
            if (code != 200 && code != HttpURLConnection.HTTP_NOT_MODIFIED && cached != null) return cached;
            if (code == HttpURLConnection.HTTP_NOT_MODIFIED && cached != null) {
                prefs.edit().putLong("at_" + key, now).apply();
                return cached;
            }
            if (code != 200) throw new IllegalStateException("fixtur.es HTTP " + code);
            String body;
            try (InputStream in = connection.getInputStream()) { body = new String(readAll(in, 3_000_000), StandardCharsets.UTF_8); }
            java.nio.file.Files.write(file.toPath(), body.getBytes(StandardCharsets.UTF_8));
            prefs.edit().remove("body_" + key).putLong("at_" + key, now)
                    .putString("lm_" + key, connection.getHeaderField("Last-Modified")).apply();
            return body;
        } finally { connection.disconnect(); }
    }

    private static String userAgent() {
        String version = "?";
        try { version = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName; }
        catch (Exception ignored) { }
        return "LinarTV/" + version + " (Android TV launcher; +https://github.com/domlin92/linartv-launcher-open)";
    }

    private static String unescape(String value) {
        return value.replace("\\,", ",").replace("\\;", ";").replace("\\n", " ").replace("\\\\", "\\");
    }

    // Crests of foreign clubs from football-logos.cc (credited in README, CRESTS.md and "About",
    // as the site asks). Images are downloaded once on the TV and cached; none are in the
    // repository or the APK. A club or the site can ask for removal - see docs/CRESTS.md.
    private static final Map<String, String> LOGOS = new java.util.concurrent.ConcurrentHashMap<>();
    /** Per country page: crest file names, display names ("Deportivo Alavés") and image URLs. */
    private static final Map<String, List<String>> LOGO_NAMES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, List<String>> LOGO_TITLES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<String, List<String>> LOGO_URLS = new java.util.concurrent.ConcurrentHashMap<>();
    /** A crest tile: the 256 px image and its alt text, e.g. alt="Deportivo Alavés logo". */
    private static final Pattern LOGO = Pattern.compile(
            "<img src=\"(https://assets\\.football-logos\\.cc/logos/([a-z-]+)/256x256/([a-z0-9-]+)\\.[0-9a-f]+\\.png)\"[^>]*?alt=\"([^\"]*)\"");

    /**
     * Crest image URL for a club from any of the five countries (cup opponents included), or
     * null. A club found in more than one country is left without a crest.
     */
    static String logoUrl(String team) {
        String cached = LOGOS.get(team);
        if (cached != null) return cached.isEmpty() ? null : cached;
        if (LOGO_NAMES.size() < COUNTRIES.size()) return null; // pages not read yet
        // Best match in each country, then the strongest across countries ("Aston Villa" fits
        // aston-villa in England better than villarreal in Spain); an exact tie gives no crest.
        String found = "";
        int[] strongest = null;
        boolean tie = false;
        for (Country c : COUNTRIES) {
            List<String> names = LOGO_NAMES.get(c.logoPage), titles = LOGO_TITLES.get(c.logoPage);
            if (names == null) continue;
            int index = CrestNameMatcher.best(team, names, titles);
            if (index < 0) continue;
            int[] strength = CrestNameMatcher.strength(team, names.get(index), titles == null ? null : titles.get(index));
            int compare = CrestNameMatcher.compareStrength(strength, strongest);
            if (compare > 0) { strongest = strength; found = LOGO_URLS.get(c.logoPage).get(index); tie = false; }
            else if (compare == 0) tie = true;
        }
        if (tie) found = "";
        LOGOS.put(team, found);
        return found.isEmpty() ? null : found;
    }

    private static final long LOGO_PAGES_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000;

    /**
     * The five country crest lists (network; call off the main thread). Each page is about
     * 1 MB, so the parsed list is kept in a small file and the page is read again after 7 days.
     */
    static synchronized void loadLogoPages() {
        if (LOGO_NAMES.size() == COUNTRIES.size()) return;
        for (Country country : COUNTRIES) {
            if (LOGO_NAMES.containsKey(country.logoPage)) continue;
            java.io.File saved = new java.io.File(app.getFilesDir(), "football-logos-" + country.logoPage + ".json");
            if (saved.isFile() && System.currentTimeMillis() - saved.lastModified() < LOGO_PAGES_MAX_AGE_MS) {
                try {
                    JSONObject json = new JSONObject(new String(java.nio.file.Files.readAllBytes(saved.toPath()), StandardCharsets.UTF_8));
                    LOGO_URLS.put(country.logoPage, strings(json.getJSONArray("urls")));
                    LOGO_TITLES.put(country.logoPage, strings(json.getJSONArray("titles")));
                    LOGO_NAMES.put(country.logoPage, strings(json.getJSONArray("names")));
                    continue;
                } catch (Exception unreadable) { saved.delete(); }
            }
            try {
                String page = readUrl("https://football-logos.cc/" + country.logoPage + "/");
                List<String> names = new ArrayList<>(), titles = new ArrayList<>(), urls = new ArrayList<>();
                Matcher m = LOGO.matcher(page);
                while (m.find()) {
                    String file = m.group(3);
                    // Skip variants such as "aston-villa--black-monochrome" and repeats.
                    if (!m.group(2).equals(country.logoPage) || file.contains("--") || names.contains(file)) continue;
                    String title = android.text.Html.fromHtml(m.group(4)).toString().replaceFirst("(?i)\\s+logo$", "").trim();
                    names.add(file); titles.add(title); urls.add(m.group(1));
                }
                LOGO_URLS.put(country.logoPage, urls);
                LOGO_TITLES.put(country.logoPage, titles);
                LOGO_NAMES.put(country.logoPage, names);
                JSONObject json = new JSONObject().put("names", new JSONArray(names))
                        .put("titles", new JSONArray(titles)).put("urls", new JSONArray(urls));
                java.nio.file.Files.write(saved.toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
            } catch (Exception unavailable) {
                android.util.Log.w("LinarTV", "Foreign crests unavailable: " + country.logoPage, unavailable);
            }
        }
        LOGOS.clear();
    }

    private static List<String> strings(JSONArray array) throws Exception {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) list.add(array.getString(i));
        return list;
    }

    private static String readUrl(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(10000);
        try {
            if (connection.getResponseCode() != 200)
                throw new IllegalStateException("HTTP " + connection.getResponseCode());
            try (InputStream in = connection.getInputStream()) {
                return new String(readAll(in, 3_000_000), StandardCharsets.UTF_8);
            }
        } finally { connection.disconnect(); }
    }

    private static byte[] readAll(InputStream in, int limit) throws Exception {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
                if (bytes.size() > limit) throw new IllegalStateException("Response too large");
            }
            return bytes.toByteArray();
        }
    }
}
