package com.pet_projects.bloodspotbotapi.client;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineFeignClient;
import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.service.jobs.SpotDonationJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@Disabled
@SpringBootTest
public class DonorMosOnlineFeignClientIntegrationTst {

    @Autowired
    private DonorMosOnlineFeignClient donorClient;

    @Autowired
    private DonorMosOnlineClient donorMosOnlineClient;

    @Autowired
    private SpotDonationJob spotDonationJob;


    @Test
    public void testAuth_realRequest2() {
        ResponseEntity<String> result = donorMosOnlineClient.auth(AuthBody.builder()
                .log("alexldomra@gmail.com")
                .pwd("BCeTZx!cso.Ko-ux-EzmMyyydo7WaX@XxBa9VvheBTAAHRLCB")
                .redirect_to("https://donor-mos.online/account")
                .build());

        System.out.println(result.getHeaders().get("Set-Cookie"));
    }


    @Test
    public void testAuth_realRequest3() {
        String response = spotDonationJob.fetchSpotsFor(
                User.builder()
                        .email("alexldomra@gmail.com")
                        .password("BCeTZx!cso.Ko-ux-EzmMyyydo7WaX@XxBa9VvheBTAAHRLCB")
                        .build()
        );
        Document document = Jsoup.parse(response);
        Elements needs = document.getElementsByClass("dates-table__item table-item");
        for (Element element : needs) {
            String rawDate = element.selectFirst("td.table-item__date").text();
            Element btn = element.selectFirst("td.table-item__btn").getElementsByTag("button").first();
            if (btn != null) {
                Attribute attribute = btn.attribute("data-src");
                if (attribute != null) {
                    attribute.getValue();
                }
            }
        }
        System.out.println(document.getElementsByAttribute("needs-table"));
    }
}