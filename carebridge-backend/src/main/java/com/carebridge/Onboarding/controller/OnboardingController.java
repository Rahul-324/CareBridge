package com.carebridge.Onboarding.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carebridge.Onboarding.Service.OnboardingService;
import com.carebridge.Onboarding.dto.request.OnboardingRequest;
import com.carebridge.Onboarding.dto.response.OnboardingResponse;
import com.carebridge.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    @PostMapping("/clinic")
    public ResponseEntity<ApiResponse<OnboardingResponse>> onboardClinic( @Valid @RequestBody OnboardingRequest request){
        OnboardingResponse onboardingResponse= onboardingService.onboardClinic(request);

        ApiResponse<OnboardingResponse> response= ApiResponse.success("clinic onboarding completed succesfully",onboardingResponse);


        return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response) ;
    }
    

}
