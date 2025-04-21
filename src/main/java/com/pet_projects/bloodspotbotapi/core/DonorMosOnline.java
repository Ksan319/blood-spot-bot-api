package com.pet_projects.bloodspotbotapi.core;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DonorMosOnline {
    private final DonorMosOnlineFeignClient donorMosOnlineFeignClient;


}
