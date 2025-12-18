package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.service.FirestationService;

@RestController
public class FirestationController {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private FirestationService firestationService;

    @GetMapping("/firestation")
    public ResponseEntity<FirestationCoverageResponseDTO> getPersonsByStation(
            @RequestParam("stationNumber") String stationNumber) {
        logger.info("[CALL] firestation?stationNumber={}", stationNumber);
        FirestationCoverageResponseDTO result = firestationService.getPersonsCoveredByStation(stationNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/phoneAlert")
    public ResponseEntity<PhoneAlertResponseDTO> getPhoneByStation(
            @RequestParam("firestation") String firestation) {
        logger.info("[CALL] phoneAlert?firestation={}", firestation);
        PhoneAlertResponseDTO result = firestationService.getPhoneOfPersonsCoveredByStation(firestation);
        return ResponseEntity.ok(result);
    }

}