package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.service.FirestationService;

@RestController
@RequestMapping("/firestation")
public class FirestationController {
    private final Logger logger = LoggerFactory.getLogger(FirestationService.class);

    private final FirestationService firestationService;

    public FirestationController(FirestationService firestationService) {
        this.firestationService = firestationService;
    }

    @PostMapping
    public ResponseEntity<Void> addMapping(@RequestBody Firestation firestation) {
        logger.info("[CALL] POST firestation -> CREATE station");
        firestationService.addMapping(firestation);
        logger.info("[RESPONSE] POST firestation -> SUCCESS, station created");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateMapping(@RequestBody Firestation firestation) {
        logger.info("[CALL] PUT firestation -> UPDATE station");
        firestationService.updateMapping(firestation);
        logger.info("[SUCCESS] PUT firestation -> station updated");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMapping(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Integer station) {

        logger.info("[CALL] PUT firestation -> DELETE station");
        firestationService.deleteMapping(address, station);
        logger.info("[SUCCESS] PUT firestation -> station deleted");
        return ResponseEntity.noContent().build();
    }
}