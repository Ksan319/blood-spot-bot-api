package com.pet_projects.bloodspotbotapi.utils;

import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public class SpotUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

    public static List<SpotDTO> getSpots(Elements elements) {
        List<SpotDTO> spots = new ArrayList<>();
        int allowed = 0;
        int forbidden = 0;
        int noButton = 0;
        int noDataSrc = 0;

        for (Element element : elements) {
            String rawDate = Objects.requireNonNull(element.selectFirst("td.table-item__date")).text();
            LocalDate localDate = LocalDate.parse(rawDate, formatter);

            Element btn = Objects.requireNonNull(element.selectFirst("td.table-item__btn")).getElementsByTag("button").first();
            if (btn == null) {
                noButton++;
                log.debug("Spot row {}: no <button> found", localDate);
                continue;
            }

            Attribute attribute = btn.attribute("data-src");
            if (attribute == null) {
                noDataSrc++;
                log.debug("Spot row {}: button without data-src", localDate);
                continue;
            }

            String dataSrc = attribute.getValue();
            boolean isAllowed = !"#modal-forbidden".equals(dataSrc);
            if (isAllowed) {
                allowed++;
                spots.add(SpotDTO.builder().spotDate(localDate).build());
                log.debug("Spot row {}: data-src='{}' → allowed", localDate, dataSrc);
            } else {
                forbidden++;
                log.debug("Spot row {}: data-src='{}' → forbidden/skipped", localDate, dataSrc);
            }
        }

        log.info("Spot parse breakdown: elements={}, allowed={}, forbidden={}, noButton={}, noDataSrc={}",
                elements != null ? elements.size() : 0, allowed, forbidden, noButton, noDataSrc);
        return spots;
    }
}
