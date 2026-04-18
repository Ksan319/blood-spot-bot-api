package com.pet_projects.bloodspotbotapi.model;

import java.util.List;

public enum UserSite {
    DONOR_MOS("https://donor-mos.online", "https://donor-mos.online/account/"),
    DONOR_MOS_SAB("https://donor-mos-sab.online", "https://donor-mos-sab.online/account/"),
    DONOR_MOS_ZAR("https://donor-mos-zar.online", "https://donor-mos-zar.online/account/"),
    ALL(null, null);

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
            case ALL -> "Все медцентры";
        };
    }

    public boolean isAll() {
        return this == ALL;
    }

    public List<UserSite> getIndividualSites() {
        if (this == ALL) {
            return List.of(DONOR_MOS, DONOR_MOS_SAB, DONOR_MOS_ZAR);
        }
        return List.of(this);
    }
}
