package com.openclassrooms.safetynetalerts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationsResponseDTO;
import com.openclassrooms.safetynetalerts.service.PersonService;

@RestController
@RequestMapping("/flood")
public class FloodController {
    private final Logger logger = LoggerFactory.getLogger(FloodController.class);

    @Autowired
    private PersonService personService;

    @GetMapping("/stations") // TODO : Nécessité de déplacer dans floodController ?
    public ResponseEntity<FloodStationsResponseDTO> getPersonsByStations(
            @RequestParam("stations") List<Integer> stations) {
        logger.info("[CALL] GET flood/station?stations={}", stations);
        FloodStationsResponseDTO result = personService.getPersonAndMedicalHistoryCoveredByStations(stations);
        logger.info("[RESPONSE] GET flood/station?stations={} -> SUCCESS", stations);

        return ResponseEntity.ok(result);
    }
}
