package com.linart.tvpreview;

/**
 * Folds Open-Meteo WMO weather codes into the seven wallpaper kinds used by
 * "Tapeta śledzi pogodę" / "Wallpaper follows the weather".
 *
 * <p>The widget icons ({@link MeteoconIcons}) draw more shapes than the wallpapers need, so every
 * code lands in one of seven kinds: drizzle, freezing rain and showers count as rain, sleet as
 * snow, hail as a thunderstorm and an unknown code as clouds - the same split the user asked for
 * (clear, partly cloudy, clouds, rain, snow, storm, fog).
 *
 * <p>Day/night does not come from the weather: the wallpaper reuses the launcher's own state from
 * "Launcher appearance" (automatic schedule, always light / always dark or custom hours), so the
 * background flips together with the rest of the interface. The seven kinds times two variants are
 * fourteen drawables, documented in {@code docs/WEATHER-WALLPAPERS.md}.
 *
 * <p>Kept free of Android imports so {@code tests/WeatherWallpaperCheck.java} can run with plain
 * javac; the resource ids live in {@code DashboardActivity#weatherWallpaperDrawable}.
 */
final class WeatherWallpaper {
    static final int CLEAR = 0;
    static final int PARTLY_CLOUDY = 1;
    static final int CLOUDY = 2;
    static final int RAIN = 3;
    static final int SNOW = 4;
    static final int STORM = 5;
    static final int FOG = 6;
    static final int KIND_COUNT = 7;

    private WeatherWallpaper() { }

    /** One of the seven kinds; clouds when the code is missing or not a WMO number. */
    static int kindOf(String code) {
        switch (wmoOf(code)) {
            case 0: case 1: return CLEAR;
            case 2: return PARTLY_CLOUDY;
            case 45: case 48: return FOG;
            case 51: case 53: case 55: case 56: case 57:
            case 61: case 63: case 65: case 66: case 67:
            case 80: case 81: case 82: return RAIN;
            case 71: case 73: case 75: case 77: case 85: case 86: return SNOW;
            case 95: case 96: case 99: return STORM;
            default: return CLOUDY;
        }
    }

    /** The WMO number in "d61" / "n61", or -1 when the code cannot be read. */
    static int wmoOf(String code) {
        if (code == null || code.length() < 2) return -1;
        char prefix = code.charAt(0);
        if (prefix != 'd' && prefix != 'n') return -1;
        try { return Integer.parseInt(code.substring(1)); }
        catch (NumberFormatException ignored) { return -1; }
    }
}
