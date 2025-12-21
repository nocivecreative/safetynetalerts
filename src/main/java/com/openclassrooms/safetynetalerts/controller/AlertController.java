package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class AlertController {
    private final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private FirestationService firestationService;

    @Autowired
    private PersonService personService;

    @GetMapping("/firestation")
    public ResponseEntity<FirestationCoverageResponseDTO> getPersonsByStation(
            @RequestParam("stationNumber") int stationNumber) {
        logger.info("[CALL] GET firestation?stationNumber={}", stationNumber);
        FirestationCoverageResponseDTO result = firestationService.getPersonsCoveredByStation(stationNumber);
        logger.info("[RESPONSE] GET firestation?stationNumber={} -> SUCCESS", stationNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/phoneAlert")
    public ResponseEntity<PhoneAlertResponseDTO> getPhoneByStation(
            @RequestParam("firestation") int firestation) {
        logger.info("[CALL] GET phoneAlert?firestation={}", firestation);
        PhoneAlertResponseDTO result = firestationService.getPhoneOfPersonsCoveredByStation(firestation);
        logger.info("[RESPONSE] GET phoneAlert?firestation={} -> SUCCESS", firestation);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertResponseDTO> getChildrenByAdress(
            @RequestParam("address") String address) {
        logger.info("[CALL] GET childAlert?address={}", address);
        ChildAlertResponseDTO result = personService.getChildrenLivingAtAdress(address);
        logger.info("[RESPONSE] GET childAlert?address={} -> SUCCESS", address);
        return ResponseEntity.ok(result);
    }

}