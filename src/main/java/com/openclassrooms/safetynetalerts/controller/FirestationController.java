package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.FirestationCoverageDTO;
import com.openclassrooms.safetynetalerts.dto.PhoneAlertDTO;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.service.FirestationService;

@RestController
public class FirestationController {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private FirestationService firestationService;

    @GetMapping("/firestation")
    public ResponseEntity<FirestationCoverageDTO> getPersonsByStation(
            @RequestParam("stationNumber") String stationNumber) {
        logger.info("[CALL] firestation?stationNumber={}", stationNumber);
        FirestationCoverageDTO result = firestationService.getPersonsCoveredByStation(stationNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/phoneAlert")
    public ResponseEntity<PhoneAlertDTO> getPhoneByStation(
            @RequestParam("stationNumber") String stationNumber) {
        logger.info("[CALL] phoneAlert?stationNumber={}", stationNumber);
        PhoneAlertDTO result = firestationService.getPhoneOfPersonsCoveredByStation(stationNumber);
        return ResponseEntity.ok(result);
    }

}