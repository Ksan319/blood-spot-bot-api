package com.pet_projects.bloodspotbotapi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {
    public static String extractJsCookieFromHtml(String html) {
        if (html != null && !html.isBlank()) {

            Pattern p = Pattern.compile("document\\.cookie\\s*=\\s*\"([^\"]+)\"");
            Matcher m = p.matcher(html);
            if (m.find()) {
                String full = m.group(1);
                int idx = full.indexOf(';');
                return idx > 0 ? full.substring(0, idx) : full;
            }
        }
        return null;
    }

    public static String extractJsRedirectFromHtml(String html) {
        if (html != null && !html.isBlank()) {
            Pattern p = Pattern.compile("document\\.location\\.href\\s*=\\s*\"([^\"]+)\"");
            Matcher m = p.matcher(html);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }
}
