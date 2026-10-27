package com.linart.tvpreview;

import java.util.Arrays;
import java.util.List;

public final class CrestNameMatcherCheck {
    public static void main(String[] args) {
        String[][] cases = {
                {"Olympique Lyonnais", "lyon", "hauts-lyonnais,lyon,lyon-la-duchere,nimes-olympique"},
                {"Stade Rennais FC 1901", "rennes", "rennes,ta-rennes,stade-de-reims"},
                {"Stade Brestois 29", "brest", "as-brest,brest,stade-briochin"},
                {"FC Internazionale Milano", "inter", "club-milano,inter,milan"},
                {"AC Milan", "milan", "club-milano,inter,milan,milazzo"},
                {"RCD Espanyol de Barcelona", "espanyol", "barcelona,espanyol"},
                {"Deportivo Alavés", "", "deportivo,deportivo-la-coruna,caudal-deportivo"},
                {"Tottenham Hotspur FC", "tottenham", "afc-totton,tottenham"},
                {"US Lecce", "lecce", "lecce,lecco"},
                {"VfB Stuttgart", "vfb-stuttgart", "stuttgarter-sc,vfb-stuttgart"},
                {"Le Havre AC", "le-havre-ac", "le-havre-ac,le-mans"},
                {"RB Leipzig", "rb-leipzig", "1-fc-lokomotive-leipzig,bsg-chemie-leipzig,rb-leipzig"},
                {"Athletic Club", "athletic-club", "athletic-club,atletico-madrid"},
                {"Real Racing Club de Santander", "racing", "racing,racing-club-ferrol"},
                {"RC Strasbourg", "rc-strasbourg-alsace", "as-strasbourg,rc-strasbourg-alsace"},
                {"Athletic de Bilbao", "athletic-club", "athletic-club,atletico-madrid"},
                {"S.S. Lazio", "lazio", "lazio,latina"},
                {"1.FC Köln", "koln", "koln,fortuna-koln"},
                {"BV Borussia 1909 (Dortmund)", "borussia-dortmund", "borussia-dortmund,borussia-monchengladbach,asc-09-dortmund"},
        };
        for (String[] c : cases) {
            List<String> files = Arrays.asList(c[2].split(","));
            int index = CrestNameMatcher.best(c[0], files);
            String got = index < 0 ? "" : files.get(index);
            if (!got.equals(c[1])) throw new AssertionError(c[0] + ": expected '" + c[1] + "', got '" + got + "'");
        }
        System.out.println("Crest name matcher checks: " + cases.length + " passed");
    }
}
