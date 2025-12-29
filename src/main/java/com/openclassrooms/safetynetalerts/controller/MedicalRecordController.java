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

import com.openclassrooms.safetynetalerts.dto.medicalrecord.MedicalRecordDTO;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/medicalRecord")
@Validated
public class MedicalRecordController {
    private final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    @Autowired
    private MedicalRecordService medicalRecordService;

    /**
     * POST /medicalRecord
     * Crée un nouveau dossier médical
     */
    @PostMapping
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(
            @Valid @RequestBody MedicalRecordDTO medicalRecordDTO) {

        logger.info("[CALL] POST /medicalRecord -> Creating medical record for {} {}",
                medicalRecordDTO.getFirstName(), medicalRecordDTO.getLastName());

        // Mapper DTO → Entity
        MedicalRecord medicalRecord = new MedicalRecord(
                medicalRecordDTO.getFirstName(),
                medicalRecordDTO.getLastName(),
                medicalRecordDTO.getBirthdate(),
                medicalRecordDTO.getMedications(),
                medicalRecordDTO.getAllergies());

        // Appeler le service
        MedicalRecord createdRecord = medicalRecordService.createMedicalRecord(medicalRecord);

        // Mapper Entity → DTO pour la réponse
        MedicalRecordDTO response = mapEntityToDto(createdRecord);

        logger.info("[RESPONSE] POST /medicalRecord -> Medical record created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /medicalRecord?firstName=<firstName>&lastName=<lastName>
     * Met à jour un dossier médical existant
     */
    @PutMapping
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @RequestParam("firstName") @NotBlank(message = "First name required") String firstName,
            @RequestParam("lastName") @NotBlank(message = "Last name required") String lastName,
            @RequestBody MedicalRecordDTO medicalRecordDTO) {

        logger.info("[CALL] PUT /medicalRecord -> Updating medical record for {} {}",
                firstName, lastName);

        // Mapper DTO → Entity
        MedicalRecord medicalRecord = new MedicalRecord(
                firstName,
                lastName,
                medicalRecordDTO.getBirthdate(),
                medicalRecordDTO.getMedications(),
                medicalRecordDTO.getAllergies());

        // Appeler le service
        MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(
                firstName, lastName, medicalRecord);

        // Mapper Entity → DTO pour la réponse
        MedicalRecordDTO response = mapEntityToDto(updatedRecord);

        logger.info("[RESPONSE] PUT /medicalRecord -> Medical record updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /medicalRecord?firstName=<firstName>&lastName=<lastName>
     * Supprime un dossier médical
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMedicalRecord(
            @RequestParam("firstName") @NotBlank(message = "First name is required") String firstName,
            @RequestParam("lastName") @NotBlank(message = "Last name is required") String lastName) {

        logger.info("[CALL] DELETE /medicalRecord -> Deleting medical record for {} {}",
                firstName, lastName);

        // Appeler le service
        medicalRecordService.deleteMedicalRecord(firstName, lastName);

        logger.info("[RESPONSE] DELETE /medicalRecord -> Medical record deleted successfully");
        return ResponseEntity.noContent().build();
    }

    // --- Méthodes privées de mapping ---

    private MedicalRecordDTO mapEntityToDto(MedicalRecord record) {
        return new MedicalRecordDTO(
                record.getFirstName(),
                record.getLastName(),
                record.getBirthdate(),
                record.getMedications(),
                record.getAllergies());
    }
}