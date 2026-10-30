package com.linart.tvpreview;

public final class ClubNamesCheck {
    public static void main(String[] args) {
        check(ClubNames.normalize(" \u015Al\u0105sk-Wroc\u0142aw ").equals("slask wroclaw"));
        check(ClubNames.normalize("\u015Al\u0105sk II Wroc\u0142aw").equals("slask ii wroclaw"));
        check(ClubNames.normalize("Deportivo Alav\u00E9s").equals("deportivo alaves"));
        check(ClubNames.normalize("Brighton & Hove Albion FC").equals("brighton hove albion fc"));
        check(ClubNames.normalize("KSZO 1929").equals("kszo 1929"));
        check(ClubNames.normalize(null).isEmpty());
        check(ClubNames.normalize("  ").isEmpty());
        System.out.println("Club name checks: 7 passed");
    }
    private static void check(boolean valid) {
        if (!valid) throw new AssertionError("Club name normalisation changed");
    }
}
