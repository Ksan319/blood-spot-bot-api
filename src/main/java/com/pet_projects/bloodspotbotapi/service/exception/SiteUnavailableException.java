package com.pet_projects.bloodspotbotapi.service.exception;

public class SiteUnavailableException extends RuntimeException {

    private final String siteName;

    public SiteUnavailableException(String siteName) {
        super("Site unavailable: " + siteName);
        this.siteName = siteName;
    }

    public SiteUnavailableException(String siteName, Throwable cause) {
        super("Site unavailable: " + siteName, cause);
        this.siteName = siteName;
    }

    public String getSiteName() {
        return siteName;
    }
}
