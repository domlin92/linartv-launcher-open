package com.linart.tvpreview;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Matches a club name ("Olympique Lyonnais") to a crest file name ("lyon").
 * Rules: (1) words match when equal, when one starts the other, or when they share a root of
 * 4+ letters (Lyonnais/Lyon, Rennais/Rennes, Internazionale/Inter); (2) generic words such as
 * "Deportivo" or "Real" never decide alone; (3) on a tie the club's first distinctive word wins,
 * then an exact word beats a shared root, then the shorter file name.
 */
final class CrestNameMatcher {
    private static final Set<String> FILLER = new HashSet<>(Arrays.asList(
            "fc", "afc", "cf", "sc", "ac", "ssc", "club", "de", "bv", "sv", "the", "cfc", "calcio",
            "as", "us", "ud", "rc", "cd", "sd", "ss", "acf", "ogc", "rcd", "la", "di", "del", "vfb",
            "vfl", "tsg", "fsv", "sco", "aj", "es", "osc", "bc"));
    private static final Set<String> GENERIC = new HashSet<>(Arrays.asList(
            "deportivo", "real", "olympique", "stade", "united", "city", "town", "athletic", "atletico",
            "sporting", "racing", "inter", "borussia", "eintracht", "hellas", "union", "dynamo", "hotspur",
            "albion", "wanderers", "rovers", "county", "balompie", "futbol", "football"));
    /** Names the rules cannot resolve. */
    private static final Map<String, String> EXCEPTIONS = new HashMap<>();
    static {
        EXCEPTIONS.put("Real Racing Club de Santander", "racing");
        // fixtur.es club names
        EXCEPTIONS.put("Racing de Santander", "racing");
        EXCEPTIONS.put("Athletic de Bilbao", "athletic-club");
        EXCEPTIONS.put("RC Strasbourg", "rc-strasbourg-alsace");
    }

    private CrestNameMatcher() { }

    /** Index of the best file name for the club, or -1 when none fits or two fit equally. */
    static int best(String club, List<String> fileNames) {
        String exception = EXCEPTIONS.get(club);
        if (exception != null) return fileNames.indexOf(exception);
        List<String> team = words(club);
        int bestIndex = -1;
        int[] bestScore = null;
        boolean tie = false;
        for (int i = 0; i < fileNames.size(); i++) {
            int[] score = score(team, words(fileNames.get(i).replace('-', ' ')), fileNames.get(i).length());
            if (score == null) continue;
            int compare = bestScore == null ? 1 : compare(score, bestScore);
            if (compare > 0) { bestIndex = i; bestScore = score; tie = false; }
            else if (compare == 0) tie = true;
        }
        return tie ? -1 : bestIndex;
    }

    static List<String> words(String name) {
        String plain = Normalizer.normalize(name.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('ł', 'l');
        List<String> result = new ArrayList<>();
        for (String w : plain.split("[^a-z0-9]+"))
            if (!w.isEmpty() && !FILLER.contains(w) && !w.matches("\\d+")) result.add(w);
        return result;
    }

    static boolean same(String a, String b) {
        if (a.equals(b)) return true;
        if (a.startsWith(b) || b.startsWith(a)) return Math.min(a.length(), b.length()) >= 3;
        int common = 0, shorter = Math.min(a.length(), b.length());
        while (common < shorter && a.charAt(common) == b.charAt(common)) common++;
        return common >= 4 && shorter >= 5 && common >= shorter - 2;
    }

    /** {distinctive words, -position of first, exact words, matched words, -file words, -file length}. */
    private static int[] score(List<String> team, List<String> file, int fileLength) {
        if (team.isEmpty() || file.isEmpty()) return null;
        List<String> matched = new ArrayList<>();
        for (String t : team) for (String f : file) if (same(t, f)) { matched.add(t); break; }
        for (String f : file) {
            boolean found = false;
            for (String t : team) if (same(t, f)) { found = true; break; }
            if (!found) return null; // the file names a word the club lacks
        }
        List<String> distinctive = new ArrayList<>();
        for (String m : matched) if (!GENERIC.contains(m)) distinctive.add(m);
        if (distinctive.isEmpty()) {
            List<String> a = new ArrayList<>(team), b = new ArrayList<>(file);
            a.sort(null); b.sort(null);
            if (!a.equals(b)) return null; // generic words alone only for an identical name
            distinctive = matched;
        }
        int exact = 0;
        for (String m : matched) if (file.contains(m)) exact++;
        return new int[]{distinctive.size(), -team.indexOf(distinctive.get(0)), exact, matched.size(),
                -file.size(), -fileLength};
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
        return 0;
    }
}
