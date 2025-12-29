package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.firestation.FirestationDTO;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.service.FirestationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/firestation")
@Validated
public class FirestationController {
    private final Logger logger = LoggerFactory.getLogger(FirestationController.class);

    @Autowired
    FirestationService firestationService;

    /**
     * POST /firestation
     * Ajoute un nouveau mapping caserne/adresse
     */
    @PostMapping
    public ResponseEntity<FirestationDTO> addMapping(@Valid @RequestBody FirestationDTO firestationDTO) {
        logger.info("[CALL] POST /firestation -> Adding mapping address={}, station={}",
                firestationDTO.getAddress(), firestationDTO.getStation());

        // Mapper DTO vers Entité
        Firestation firestation = new Firestation(
                firestationDTO.getAddress(),
                firestationDTO.getStation());

        // Appeler le service
        firestationService.addMapping(firestation);

        // Mapper DTO pour la réponse
        FirestationDTO response = mapEntityToDto(firestation);

        logger.info("[RESPONSE] POST /firestation -> Mapping created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /firestation
     * <p>
     * Met à jour le numéro de station pour une adresse
     * 
     * @param address        l'adresse de la caserne à supprimer
     * @param firestationDTO le DTO caserne
     */
    @PutMapping
    public ResponseEntity<FirestationDTO> updateStation(
            @RequestParam("address") @NotBlank(message = "address is required") String address,
            @RequestBody FirestationDTO firestationDTO) {
        logger.info("[CALL] PUT /firestation -> Updating mapping address={}, new station number={}",
                address, firestationDTO.getStation());

        // Mapper DTO vers Entité
        Firestation firestation = new Firestation(address, firestationDTO.getStation());

        // Appeler le service
        firestationService.updateMapping(address, firestation);

        // Mapper Entité vers DTO pour la réponse
        FirestationDTO response = mapEntityToDto(firestation);

        logger.info("[RESPONSE] PUT /firestation -> Mapping updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /firestation?address="adresse" OU /firestation?station="numéro"
     * 
     * <p>
     * Supprime un mapping (soit par adresse, soit par numéro de caserne)
     * 
     * @param address l'adresse de la caserne à supprimer (optional)
     * @param station le numéro de la caserne à supprimer (optional)
     * @return ResponseEntity sans contenu (HTTP 204) en cas de suppression réussie
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMapping(
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "station", required = false) Integer station) {

        logger.info("[CALL] DELETE /firestation -> address={}, station={}", address, station);

        // Appeler le service
        firestationService.deleteMapping(address, station);

        logger.info("[RESPONSE] DELETE /firestation -> Mapping deleted successfully");
        return ResponseEntity.noContent().build();
    }

    // --- Méthodes privées de mapping ---

    private FirestationDTO mapEntityToDto(Firestation fs) {
        return new FirestationDTO(
                fs.getAddress(),
                fs.getStation());
    }
}