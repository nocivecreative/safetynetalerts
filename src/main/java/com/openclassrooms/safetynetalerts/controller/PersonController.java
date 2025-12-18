package com.openclassrooms.safetynetalerts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.ChildAlertDTO;
import com.openclassrooms.safetynetalerts.dto.FireDTO;
import com.openclassrooms.safetynetalerts.dto.FloodDTO;
import com.openclassrooms.safetynetalerts.dto.PersonInfolastNameDTO;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
public class PersonController {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    PersonService personService;

    @GetMapping("/childAlert")
    public ResponseEntity<ChildAlertDTO> getChildrenByAdress(
            @RequestParam("address") String address) {
        logger.info("[CALL] childAlert?address={}", address);
        ChildAlertDTO result = personService.getChildrenLivingAtAdress(address);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/fire")
    public ResponseEntity<FireDTO> getPersonsByAdress(
            @RequestParam("address") String address) {
        logger.info("[CALL] fire?address={}", address);
        FireDTO result = personService.getPersonAndMedicalHistoryLivingAtAdress(address);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/flood/stations")
    public ResponseEntity<List<FloodDTO>> getPersonsByStations(
            @RequestParam("stations") List<String> stations) {
        logger.info("[CALL] flood/station?stations={}", stations);
        List<FloodDTO> result = personService.getPersonAndMedicalHistoryCoveredByStations(stations);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/personInfolastName")
    public ResponseEntity<List<PersonInfolastNameDTO>> getPersonsByStations(
            @RequestParam("lastName") String lastName) {
        logger.info("[CALL] personInfolastName?lastName={}", lastName);
        List<PersonInfolastNameDTO> result = personService.getPersonInfosAndMedicalHistoryByLastName(lastName);

        return ResponseEntity.ok(result);
    }

}
