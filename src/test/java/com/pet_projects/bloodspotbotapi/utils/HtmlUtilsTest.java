package com.pet_projects.bloodspotbotapi.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class HtmlUtilsTest {
    
    @Test
    public void testExtractJsCookieFromHtml() throws IOException {
        String html = new String(Files.readAllBytes(Paths.get("src/test/recources/example.html")));
        String jsCookie = HtmlUtils.extractJsCookieFromHtml(html);
        assertEquals("bpc=3773579f560bc810bff8bd13a9a58620", jsCookie);
    }

    @Test
    public void testExtractJsRedirectFromHtml() throws IOException {
        String html = new String(Files.readAllBytes(Paths.get("src/test/recources/example.html")));
        String jsRedirect = HtmlUtils.extractJsRedirectFromHtml(html);
        assertEquals("http://donor-mos-sab.online/auth.php", jsRedirect);
    }

    @Test
    public void testExtractJsCookieFromHtml_null() {
        String jsCookie = HtmlUtils.extractJsCookieFromHtml(null);
        assertNull(jsCookie);
    }

    @Test
    public void testExtractJsRedirectFromHtml_null() {
        String jsRedirect = HtmlUtils.extractJsRedirectFromHtml(null);
        assertNull(jsRedirect);
    }
}
    