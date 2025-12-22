package com.openclassrooms.safetynetalerts.controller;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResult;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.HouseholdMemberDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResult;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationResidentDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.service.PersonService;
import com.openclassrooms.safetynetalerts.utils.Utils;

@RestController
public class AlertController {
    private final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    private FirestationService firestationService;

    @Autowired
    private PersonService personService;

    @Autowired
    private Utils utils;

    @GetMapping("/firestation")
    public ResponseEntity<FirestationCoverageResponseDTO> getPersonsByStation(
            @RequestParam("stationNumber") int stationNumber) {

        logger.info("[CALL] GET firestation?stationNumber={}", stationNumber);

        FirestationCoverageResult result = firestationService.getCoverageByStation(stationNumber);

        List<FirestationResidentDTO> residents = result.getPersons().stream()
                .map(p -> new FirestationResidentDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAddress(),
                        p.getPhone()))
                .toList();

        FirestationCoverageResponseDTO response = new FirestationCoverageResponseDTO(
                residents,
                result.getAdultCount(),
                result.getChildCount());

        logger.info("[RESPONSE] GET firestation?stationNumber={} -> SUCCESS", stationNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phoneAlert")
    public ResponseEntity<PhoneAlertResponseDTO> getPhoneByStation(
            @RequestParam("firestation") int firestation) {

        logger.info("[CALL] GET phoneAlert?firestation={}", firestation);

        Set<String> phones = firestationService.getPhonesByStation(firestation);

        return ResponseEntity.ok(new PhoneAlertResponseDTO(phones));
    }

    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertResponseDTO> getChildrenByAdress(
            @RequestParam("address") String address) {

        logger.info("[CALL] GET childAlert?address={}", address);

        ChildAlertResult result = personService.getPersonByAddress(address);

        List<HouseholdMemberDTO> household = result.getAdults().stream()
                .map(p -> new HouseholdMemberDTO(p.getFirstName(), p.getLastName()))
                .toList();

        List<ChildInfoDTO> children = result.getChildren().stream()
                .map(p -> new ChildInfoDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        utils.calculateAge(p),
                        household))
                .toList();

        logger.info("[RESPONSE] GET childAlert?address={} -> SUCCESS", address);
        return ResponseEntity.ok(new ChildAlertResponseDTO(children));
    }

}