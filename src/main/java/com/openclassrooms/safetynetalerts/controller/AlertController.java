package com.openclassrooms.safetynetalerts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationsResponseDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class AlertController {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private FirestationService firestationService;

    @Autowired
    private PersonService personService;

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

    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertResponseDTO> getChildrenByAdress(
            @RequestParam("address") String address) {
        logger.info("[CALL] childAlert?address={}", address);
        ChildAlertResponseDTO result = personService.getChildrenLivingAtAdress(address);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/flood/stations") // TODO : Nécessité de déplacer dans floodController ?
    public ResponseEntity<FloodStationsResponseDTO> getPersonsByStations(
            @RequestParam("stations") List<String> stations) {
        logger.info("[CALL] flood/station?stations={}", stations);
        FloodStationsResponseDTO result = personService.getPersonAndMedicalHistoryCoveredByStations(stations);

        return ResponseEntity.ok(result);
    }

}