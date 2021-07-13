package com.board.wars.util;

public class Utilities {
    static public String resolveRoute(String link, String... params) {
        for (int i = 0; i < params.length; i++) {
            String factor = "{" + i + "}";
            link = link.replace(factor, params[i]);
        }
        return link;
    }
}
