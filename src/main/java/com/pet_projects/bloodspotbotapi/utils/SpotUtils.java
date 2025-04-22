package com.pet_projects.bloodspotbotapi.utils;

import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SpotUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

    public static List<SpotDTO> getSpots(Elements elements) {
        List<SpotDTO> spots = new ArrayList<>();
        for (Element element : elements) {
            String rawDate = Objects.requireNonNull(element.selectFirst("td.table-item__date")).text();
            LocalDate localDate = LocalDate.parse(rawDate, formatter);

            Element btn = Objects.requireNonNull(element.selectFirst("td.table-item__btn")).getElementsByTag("button").first();
            if (btn != null) {
                Attribute attribute = btn.attribute("data-src");
                if (attribute != null && !attribute.getValue().equals("#modal-forbidden")) {
                    spots.add(SpotDTO.builder().spotDate(localDate).build());
                }
            }
        }
        return spots;
    }
}
