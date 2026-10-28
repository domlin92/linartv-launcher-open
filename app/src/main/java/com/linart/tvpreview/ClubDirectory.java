package com.linart.tvpreview;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Every club the wizard offers, for "search club by name": foreign clubs from the bundled
 * fixtur.es list, Polish clubs from kalendarzeligowe.pl. The Polish list takes one request per
 * league (about 23), so it is kept in a file and read again only after 7 days.
 */
final class ClubDirectory {
    static final class Club {
        final String name, league, leagueSlug, url;
        Club(String name, String league, String leagueSlug, String url) {
            this.name = name; this.league = league; this.leagueSlug = leagueSlug; this.url = url;
        }
    }

    private static final long MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000;
    private static List<Club> clubs;

    private ClubDirectory() { }

    /** All clubs (network on the first call; call off the main thread). */
    static synchronized List<Club> all(Context context) throws Exception {
        if (clubs != null) return clubs;
        List<Club> result = new ArrayList<>(polish(context));
        for (FixturesClient.Country country : FixturesClient.COUNTRIES)
            for (FixturesClient.Team team : FixturesClient.teams(country.code))
                result.add(new Club(team.name, country.league, "fixtures-" + country.code,
                        FixturesClient.feedUrl(team.slug)));
        clubs = result;
        return result;
    }

    /** Clubs whose name contains the query (accents and case ignored); name starts first. */
    static List<Club> search(List<Club> all, String query, int limit) {
        String q = CrestNameIndex.normalize(query);
        List<Club> starts = new ArrayList<>(), contains = new ArrayList<>();
        if (q.isEmpty()) return starts;
        for (Club club : all) {
            String name = CrestNameIndex.normalize(club.name);
            if (name.startsWith(q) || (" " + name).contains(" " + q)) starts.add(club);
            else if (name.contains(q)) contains.add(club);
        }
        starts.addAll(contains);
        return starts.size() > limit ? new ArrayList<>(starts.subList(0, limit)) : starts;
    }

    private static List<Club> polish(Context context) throws Exception {
        File saved = new File(context.getFilesDir(), "club-directory-pl.json");
        if (saved.isFile() && System.currentTimeMillis() - saved.lastModified() < MAX_AGE_MS) {
            try { return read(new String(Files.readAllBytes(saved.toPath()), StandardCharsets.UTF_8)); }
            catch (Exception unreadable) { saved.delete(); }
        }
        List<LeagueCatalogClient.League> leagues = LeagueCatalogClient.leagues();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        List<Club> result = new ArrayList<>();
        try {
            List<Future<List<LeagueCatalogClient.Team>>> jobs = new ArrayList<>();
            for (LeagueCatalogClient.League league : leagues)
                jobs.add(pool.submit(() -> LeagueCatalogClient.teams(league)));
            for (int i = 0; i < leagues.size(); i++) {
                LeagueCatalogClient.League league = leagues.get(i);
                List<LeagueCatalogClient.Team> teams;
                try { teams = jobs.get(i).get(); } catch (Exception skipped) { continue; }
                for (LeagueCatalogClient.Team team : teams)
                    result.add(new Club(team.name, league.name, league.slug, team.calendarUrl(league)));
            }
        } finally { pool.shutdown(); }
        if (result.isEmpty()) throw new IllegalStateException("No Polish clubs");
        JSONArray json = new JSONArray();
        for (Club c : result) json.put(new JSONObject().put("n", c.name).put("l", c.league)
                .put("s", c.leagueSlug).put("u", c.url));
        Files.write(saved.toPath(), json.toString().getBytes(StandardCharsets.UTF_8));
        return result;
    }

    private static List<Club> read(String text) throws Exception {
        JSONArray json = new JSONArray(text);
        List<Club> result = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject o = json.getJSONObject(i);
            result.add(new Club(o.getString("n"), o.getString("l"), o.getString("s"), o.getString("u")));
        }
        return result;
    }
}
