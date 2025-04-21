package com.pet_projects.bloodspotbotapi.client.donormos;

import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "https://donor-mos.online", name = "donor-mos.online" )
public interface DonorMosOnlineFeignClient {

    @PostMapping(value = "/auth.php", consumes = "application/x-www-form-urlencoded")
    Response auth(@RequestBody AuthBody authBody);
}
