package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Data
public class AuthService {

    private final DonorMosOnlineClient donorMosOnlineClient;

    @Value("${donor-source.valid-location}")
    private String validLocation;
    private final UserRepository userRepository;


    public boolean isCredentialValid(String username, String password) {
        ResponseEntity<?> response = processAuth(username, password);
        return response.getStatusCode() == HttpStatus.FOUND && response.getHeaders().getLocation().toString().equals(validLocation);
    }

    public ResponseEntity<String> processAuth(String username, String password) {
        return donorMosOnlineClient.auth(
                AuthBody.builder()
                        .log(username)
                        .pwd(password)
                        .build()
        );
    }
}
