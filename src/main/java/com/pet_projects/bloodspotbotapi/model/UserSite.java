package com.pet_projects.bloodspotbotapi.model;

public enum UserSite {
    DONOR_MOS("https://donor-mos.online", "https://donor-mos.online/account/"),
    DONOR_MOS_SAB("https://donor-mos-sab.online", "https://donor-mos-sab.online/account/"),
    DONOR_MOS_ZAR("https://donor-mos-zar.online", "https://donor-mos-zar.online/account/");

    private final String baseUrl;
    private final String validLocation;

    UserSite(String baseUrl, String validLocation) {
        this.baseUrl = baseUrl;
        this.validLocation = validLocation;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getValidLocation() {
        return validLocation;
    }

    public String getDisplayName() {
        return switch (this) {
            case DONOR_MOS -> "Поликарпова";
            case DONOR_MOS_SAB -> "Шаболовка";
            case DONOR_MOS_ZAR -> "Царицыно";
        };
    }
}
