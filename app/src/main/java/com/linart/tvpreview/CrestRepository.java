package com.linart.tvpreview;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Club crests from kalendarzeligowe.pl (/api/teams/{id}/logo), cached on the TV per team id.
 * The API gives every team a crest version (crestVer); a crest is downloaded again only when
 * that version changes, or after 7 days when the version is unknown.
 */
final class CrestRepository {
    interface Listener { void onCrestChanged(String name); }
    private static final long REFRESH_MS = 7L * 24 * 60 * 60 * 1000;
    private static final long RETRY_MS = 5L * 60 * 1000;
    private static final long MISSING_MS = 12L * 60 * 60 * 1000;
    private static final String API = "https://kalendarzeligowe.pl/api/teams";
    private final Context context;
    private final SharedPreferences prefs;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    private final Map<Integer, Entry> byId = new HashMap<>();
    private final Map<String, Entry> byName = new HashMap<>();
    private final Map<Integer, Long> versions = new HashMap<>();
    private volatile boolean closed;
    private static final class Entry {
        final String name;
        final Set<String> watchers = new HashSet<>();
        int id;
        Bitmap bitmap;
        boolean loading;
        long nextCheck, version;
        Entry(String name, int id) { this.name = name; this.id = id; }
    }
    CrestRepository(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        prefs = this.context.getSharedPreferences("club_crests", Context.MODE_PRIVATE);
    }

    /** Crest versions reported with the fixtures; a changed version replaces the cached image. */
    void noteVersions(List<IcsMatchParser.Match> matches) {
        for (IcsMatchParser.Match match : matches) {
            if (match.homeTeamId > 0 && match.homeCrestVer > 0) versions.put(match.homeTeamId, match.homeCrestVer);
            if (match.awayTeamId > 0 && match.awayCrestVer > 0) versions.put(match.awayTeamId, match.awayCrestVer);
        }
    }

    Bitmap get(String name, int id) { return get(name, id, false); }
    private Bitmap get(String name, int id, boolean force) {
        if (closed) return null;
        String normalized = CrestNameIndex.normalize(name);
        if (id <= 0) id = prefs.getInt("club_id_" + normalized, 0);
        Entry e = id > 0 ? byId.get(id) : byName.get(normalized);
        if (e == null) {
            e = new Entry(name, id);
            if (id > 0) byId.put(id, e); else byName.put(normalized, e);
        }
        e.watchers.add(name);
        long known = e.id > 0 ? versions.getOrDefault(e.id, 0L) : 0L;
        boolean newVersion = known > 0 && known != e.version && e.bitmap != null;
        // A known, unchanged version needs no forced download.
        if (force && known > 0 && known == e.version && e.bitmap != null) force = false;
        long now = System.currentTimeMillis();
        if (!e.loading && (e.bitmap == null || newVersion || force) && (force || newVersion || now >= e.nextCheck))
            request(e, force || newVersion);
        return e.bitmap;
    }
    private File cacheFile(int id) {
        File dir = new File(context.getFilesDir(), "crests-by-team");
        dir.mkdirs();
        return new File(dir, "team-" + id + ".img");
    }
    private void request(Entry e, boolean force) {
        e.loading = true;
        int id = e.id;
        long version = id > 0 ? versions.getOrDefault(id, 0L) : 0L;
        try { workers.execute(() -> load(e, id, version, force)); }
        catch (RejectedExecutionException stopped) { e.loading = false; }
    }
    private void load(Entry e, int id, long version, boolean force) {
        try {
            if (closed) return;
            // football-logos.cc only for clubs from fixtur.es feeds (no kalendarzeligowe.pl id);
            // Polish clubs without a crest keep their initials.
            if (id <= 0 && loadForeignLogo(e)) return;
            if (id <= 0) id = findTeamId(e.name);
            // No crest for this name (e.g. foreign clubs): ask again only after 12 h.
            if (id <= 0) { finish(e, 0, null, 0, MISSING_MS, "missing"); return; }
            File cached = cacheFile(id);
            Bitmap image = cached.isFile() ? decode(new java.io.FileInputStream(cached)) : null;
            long now = System.currentTimeMillis(), downloaded = prefs.getLong("downloaded_" + id, 0);
            long cachedVersion = prefs.getLong("version_" + id, 0);
            if (image != null) {
                boolean current = version > 0 ? version == cachedVersion : now - downloaded < REFRESH_MS;
                if (current && !force) { finish(e, id, image, cachedVersion, REFRESH_MS, "disk"); return; }
                publish(e, id, image, cachedVersion, "disk");
            }
            if (!force && now < prefs.getLong("retry_" + id, 0)) {
                finish(e, id, null, cachedVersion, RETRY_MS, "cooldown"); return;
            }
            prefs.edit().putLong("retry_" + id, now + RETRY_MS).apply();
            byte[] bytes = fetch(API + "/" + id + "/logo" + (version > 0 ? "?v=" + version : ""), 5_000_000);
            image = decode(new ByteArrayInputStream(bytes));
            if (image == null) throw new IllegalStateException("Unsupported crest image");
            File temporary = new File(cached.getParentFile(), cached.getName() + ".tmp");
            try {
                Files.write(temporary.toPath(), bytes);
                Files.move(temporary.toPath(), cached.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally { if (temporary.isFile()) temporary.delete(); }
            prefs.edit().putLong("downloaded_" + id, now).putLong("version_" + id, version)
                    .putInt("club_id_" + CrestNameIndex.normalize(e.name), id).apply();
            finish(e, id, image, version, REFRESH_MS, "network");
        } catch (Exception unavailable) {
            // Most IV liga teams have no crest yet (404): ask again after 12 h, not every 5 min.
            boolean noCrest = String.valueOf(unavailable.getMessage()).equals("HTTP 404");
            long delay = noCrest ? MISSING_MS : RETRY_MS;
            if (noCrest && id > 0) prefs.edit().putLong("retry_" + id, System.currentTimeMillis() + delay).apply();
            finish(e, id, null, e.version, delay, noCrest ? "no-crest" : "unavailable");
        }
    }
    /**
     * Foreign clubs' crests from football-logos.cc (see FixturesClient.logoUrl), credited as the site asks.
     * The address contains a content hash, so a cached file never needs refreshing.
     */
    private boolean loadForeignLogo(Entry e) throws Exception {
        String key = "logo2_url_" /* v2: matched by file and display name */ + CrestNameIndex.normalize(e.name);
        String url = prefs.getString(key, null);
        if (url == null) {
            FixturesClient.loadLogoPages();
            url = FixturesClient.logoUrl(e.name);
        }
        if (url == null) return false;
        File dir = new File(context.getFilesDir(), "crests-foreign");
        dir.mkdirs();
        File cached = new File(dir, Integer.toHexString(url.hashCode()) + ".png");
        Bitmap image = cached.isFile() ? decode(new java.io.FileInputStream(cached)) : null;
        if (image == null) {
            byte[] bytes = fetch(url, 2_000_000);
            image = decode(new ByteArrayInputStream(bytes));
            if (image == null) return false;
            Files.write(cached.toPath(), bytes);
            prefs.edit().putString(key, url).apply();
        }
        finish(e, e.id, image, e.version, Long.MAX_VALUE / 4, "football-logos");
        return true;
    }

    private void publish(Entry e, int id, Bitmap image, long version, String source) {
        main.post(() -> {
            if (closed) { if (image != null && image != e.bitmap) image.recycle(); return; }
            if (id > 0) { e.id = id; byId.put(id, e); }
            if (image != null) {
                e.bitmap = image;
                e.version = version;
                for (String name : e.watchers) listener.onCrestChanged(name);
            }
            if ((context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0)
                android.util.Log.d("LinarTVCrests", "source=" + source + " teamId=" + id + " version=" + version);
        });
    }
    private void finish(Entry e, int id, Bitmap image, long version, long delay, String source) {
        main.post(() -> {
            e.loading = false;
            e.nextCheck = System.currentTimeMillis() + delay;
        });
        publish(e, id, image, version, source);
    }
    void refreshIfDue(List<IcsMatchParser.Match> first, List<IcsMatchParser.Match> second, boolean forced) {
        noteVersions(first); noteVersions(second);
        prefetch(first, forced); prefetch(second, forced);
    }
    private void prefetch(List<IcsMatchParser.Match> matches, boolean forced) {
        int count = 0;
        long now = System.currentTimeMillis();
        for (IcsMatchParser.Match match : matches) {
            if (match.startsAt + 105L * 60 * 1000 < now) continue;
            get(match.home, match.homeTeamId, forced);
            get(match.away, match.awayTeamId, forced);
            if (++count == 2) break;
        }
    }
    /** Only for matches without a team id (old ICS caches): exact name match with a crest. */
    private int findTeamId(String name) throws Exception {
        JSONArray rows = new JSONArray(new String(fetch(API + "?search=" + URLEncoder.encode(name, "UTF-8"), 100_000), StandardCharsets.UTF_8));
        int found = 0;
        String normalized = CrestNameIndex.normalize(name);
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            if (!row.optBoolean("hasLogo") || !normalized.equals(CrestNameIndex.normalize(row.optString("name")))) continue;
            int id = row.optInt("id", 0);
            if (found > 0 && found != id) return 0;
            found = id;
        }
        return found;
    }
    void close() {
        closed = true;
        workers.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }

    private static Bitmap decode(InputStream stream) throws Exception {
        try (InputStream input = stream) {
            byte[] data = read(input, 5_000_000);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
            if (bounds.outWidth < 16 || bounds.outHeight < 16 || bounds.outWidth > 16000 || bounds.outHeight > 16000)
                return null;
            BitmapFactory.Options scaled = new BitmapFactory.Options();
            scaled.inSampleSize = 1;
            scaled.inScaled = false;
            scaled.inPreferredConfig = Bitmap.Config.ARGB_8888;
            while (Math.max(bounds.outWidth, bounds.outHeight) / scaled.inSampleSize > 512)
                scaled.inSampleSize *= 2;
            return BitmapFactory.decodeByteArray(data, 0, data.length, scaled);
        }
    }

    private static byte[] fetch(String url, int limit) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android TV) AppleWebKit/537.36 Chrome/128.0.0.0 Safari/537.36");
        connection.setRequestProperty("Referer", "https://kalendarzeligowe.pl/");
        try {
            if (connection.getResponseCode() != 200) throw new IllegalStateException("HTTP " + connection.getResponseCode());
            return read(connection.getInputStream(), limit);
        } finally { connection.disconnect(); }
    }

    private static byte[] read(InputStream stream, int limit) throws Exception {
        try (InputStream input = stream; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
                if (bytes.size() > limit) throw new IllegalStateException("Crest response too large");
            }
            return bytes.toByteArray();
        }
    }
}
