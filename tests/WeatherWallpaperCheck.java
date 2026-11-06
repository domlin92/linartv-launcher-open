package com.linart.tvpreview;

public final class WeatherWallpaperCheck {
    public static void main(String[] args) {
        // Clear sky and "mostly clear" share one wallpaper.
        check(WeatherWallpaper.kindOf("d0") == WeatherWallpaper.CLEAR);
        check(WeatherWallpaper.kindOf("n1") == WeatherWallpaper.CLEAR);
        check(WeatherWallpaper.kindOf("d2") == WeatherWallpaper.PARTLY_CLOUDY);
        check(WeatherWallpaper.kindOf("n3") == WeatherWallpaper.CLOUDY);
        // Fog, drizzle, freezing rain, rain, showers, sleet, snow, hail and storms.
        check(WeatherWallpaper.kindOf("d45") == WeatherWallpaper.FOG);
        check(WeatherWallpaper.kindOf("n48") == WeatherWallpaper.FOG);
        check(WeatherWallpaper.kindOf("d51") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("d56") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("n63") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("d65") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("n67") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("d80") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("d82") == WeatherWallpaper.RAIN);
        check(WeatherWallpaper.kindOf("d71") == WeatherWallpaper.SNOW);
        check(WeatherWallpaper.kindOf("n75") == WeatherWallpaper.SNOW);
        check(WeatherWallpaper.kindOf("d77") == WeatherWallpaper.SNOW);
        check(WeatherWallpaper.kindOf("d85") == WeatherWallpaper.SNOW);
        check(WeatherWallpaper.kindOf("d86") == WeatherWallpaper.SNOW);
        check(WeatherWallpaper.kindOf("n95") == WeatherWallpaper.STORM);
        check(WeatherWallpaper.kindOf("d96") == WeatherWallpaper.STORM);
        check(WeatherWallpaper.kindOf("n99") == WeatherWallpaper.STORM);
        // Unknown or damaged codes fall back to clouds instead of hiding the wallpaper.
        check(WeatherWallpaper.kindOf("d99x") == WeatherWallpaper.CLOUDY);
        check(WeatherWallpaper.kindOf("x3") == WeatherWallpaper.CLOUDY);
        check(WeatherWallpaper.kindOf("3") == WeatherWallpaper.CLOUDY);
        check(WeatherWallpaper.kindOf("") == WeatherWallpaper.CLOUDY);
        check(WeatherWallpaper.kindOf(null) == WeatherWallpaper.CLOUDY);
        // Day and night come from the launcher appearance, so the "d"/"n" prefix only has to be
        // accepted here, not interpreted.
        check(WeatherWallpaper.wmoOf("d61") == 61);
        check(WeatherWallpaper.wmoOf("n2") == 2);
        check(WeatherWallpaper.wmoOf("d") == -1);
        check(WeatherWallpaper.wmoOf("q12") == -1);
        // Every kind has a name in the gallery hint.
        check(WeatherWallpaper.KIND_COUNT == 7);
        check(WeatherWallpaper.CLEAR == 0 && WeatherWallpaper.FOG == 6);
        System.out.println("Weather wallpaper checks: 32 passed");
    }
    private static void check(boolean valid) {
        if (!valid) throw new AssertionError("Weather wallpaper mapping changed");
    }
}
