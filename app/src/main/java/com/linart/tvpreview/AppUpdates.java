package com.linart.tvpreview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * "Update available": once a day the app asks GitHub for the latest LinarTV Open release
 * (our own repository) and, when it is newer, can download LinarTV.apk for Android's own
 * install window. Nothing is installed without the user pressing "Install" there.
 */
final class AppUpdates {
    static final class Release {
        final String version, notes, apkUrl;
        final long size;
        Release(String version, String notes, String apkUrl, long size) {
            this.version = version; this.notes = notes; this.apkUrl = apkUrl; this.size = size;
        }
    }

    private static final String LATEST = "https://api.github.com/repos/domlin92/linartv-launcher-open/releases/latest";
    private static final long CHECK_EVERY_MS = 24L * 60 * 60 * 1000;

    private AppUpdates() { }

    /** Installed version; debug builds can pretend to be older ("update_test_version") for testing. */
    static String installedVersion(Context context) {
        String version = "0";
        try { version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName; }
        catch (Exception ignored) { }
        if ((context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            version = prefs(context).getString("update_test_version", version);
        return version;
    }

    /**
     * The newer release, or null. Asks GitHub at most once a day (network; call off the main
     * thread); in between the last answer is reused.
     */
    static Release check(Context context, boolean force) {
        SharedPreferences prefs = prefs(context);
        // Only copies installed from GitHub Releases are offered updates. Test builds are signed
        // with another key; Android would refuse the release over them, so they never see the bar
        // (a debug build can still test the bar with "update_test_version").
        if (!releaseSigned(context) && !prefs.contains("update_test_version")) return null;
        long now = System.currentTimeMillis();
        String json = prefs.getString("latest_json", null);
        if (force || json == null || now - prefs.getLong("checked_at", 0) >= CHECK_EVERY_MS) {
            try {
                json = read(LATEST, context);
                prefs.edit().putString("latest_json", json).putLong("checked_at", now).apply();
            } catch (Exception offline) {
                android.util.Log.w("LinarTV", "Update check failed", offline);
            }
        }
        if (json == null) return null;
        try {
            JSONObject release = new JSONObject(json);
            String version = release.optString("tag_name", "").replaceFirst("^[vV]", "");
            if (compare(version, installedVersion(context)) <= 0) return null;
            JSONArray assets = release.optJSONArray("assets");
            if (assets == null) return null;
            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                if ("LinarTV.apk".equals(asset.optString("name")))
                    return new Release(version, release.optString("body", ""),
                            asset.getString("browser_download_url"), asset.optLong("size"));
            }
        } catch (Exception broken) {
            android.util.Log.w("LinarTV", "Update info unreadable", broken);
        }
        return null;
    }

    /** "Later": hide the bar for this version until tomorrow. */
    static void postpone(Context context, Release release) {
        prefs(context).edit().putString("later_version", release.version)
                .putLong("later_until", System.currentTimeMillis() + CHECK_EVERY_MS).apply();
    }

    static boolean postponed(Context context, Release release) {
        SharedPreferences prefs = prefs(context);
        return release.version.equals(prefs.getString("later_version", ""))
                && System.currentTimeMillis() < prefs.getLong("later_until", 0);
    }

    interface Progress { void update(long done, long total); }

    /** Downloads the APK into the app's cache (network; call off the main thread). */
    static File download(Context context, Release release, Progress progress) throws Exception {
        File dir = new File(context.getCacheDir(), "updates");
        dir.mkdirs();
        File apk = new File(dir, "LinarTV-" + release.version + ".apk");
        HttpURLConnection connection = (HttpURLConnection) new URL(release.apkUrl).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", userAgent(context));
        try {
            if (connection.getResponseCode() != 200) throw new IllegalStateException("HTTP " + connection.getResponseCode());
            long total = connection.getContentLengthLong() > 0 ? connection.getContentLengthLong() : release.size;
            try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(apk)) {
                byte[] buffer = new byte[16384];
                long done = 0;
                int count;
                while ((count = in.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    out.write(buffer, 0, count);
                    done += count;
                    progress.update(done, total);
                }
            }
        } catch (Exception failed) {
            apk.delete();
            throw failed;
        } finally { connection.disconnect(); }
        return apk;
    }

    /** True when the downloaded APK is this app, signed with the same key (else Android refuses it). */
    @SuppressWarnings("deprecation")
    static boolean sameSigner(Context context, File apk) {
        PackageManager pm = context.getPackageManager();
        PackageInfo archive = pm.getPackageArchiveInfo(apk.getAbsolutePath(), PackageManager.GET_SIGNATURES);
        if (archive == null || !context.getPackageName().equals(archive.packageName)) return false;
        try {
            Signature[] installed = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            return Arrays.equals(installed, archive.signatures);
        } catch (Exception e) { return false; }
    }

    /** SHA-256 of the certificate that signs GitHub releases (also printed in each release's notes). */
    private static final String RELEASE_CERT_SHA256 = "4ec7ad52b64f86e19e3c91790c1276c49fe641c3c20db56454669821b4e0ad4c";

    @SuppressWarnings("deprecation")
    static boolean releaseSigned(Context context) {
        try {
            Signature[] signatures = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            if (signatures == null || signatures.length == 0) return false;
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(signatures[0].toByteArray());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return RELEASE_CERT_SHA256.equals(hex.toString());
        } catch (Exception e) { return false; }
    }

    /** -1, 0 or 1 for "1.0.2" against "1.0.10" style versions. */
    static int compare(String a, String b) {
        String[] x = a.split("[.-]"), y = b.split("[.-]");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? number(x[i]) : 0, q = i < y.length ? number(y[i]) : 0;
            if (p != q) return p < q ? -1 : 1;
        }
        return 0;
    }

    private static int number(String part) {
        try { return Integer.parseInt(part.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0; }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("app_updates", Context.MODE_PRIVATE);
    }

    private static String userAgent(Context context) {
        return "LinarTV/" + installedVersion(context) + " (Android TV launcher; +https://github.com/domlin92/linartv-launcher-open)";
    }

    private static String read(String address, Context context) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", userAgent(context));
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        try {
            if (connection.getResponseCode() != 200) throw new IllegalStateException("HTTP " + connection.getResponseCode());
            try (InputStream in = connection.getInputStream()) {
                java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1 && bytes.size() < 500_000) bytes.write(buffer, 0, count);
                return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            }
        } finally { connection.disconnect(); }
    }
}
