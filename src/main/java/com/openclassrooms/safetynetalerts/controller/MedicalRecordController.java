package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.openclassrooms.safetynetalerts.mapper.MedicalRecordMapper;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrôleur REST pour la gestion des dossiers médicaux.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour gérer les dossiers médicaux
 * des personnes (date de naissance, médicaments, allergies) :
 * <ul>
 * <li>POST /medicalRecord - Création d'un nouveau dossier médical</li>
 * <li>PUT /medicalRecord - Mise à jour d'un dossier médical existant</li>
 * <li>DELETE /medicalRecord - Suppression d'un dossier médical</li>
 * </ul>
 *
 */
@RestController
@RequestMapping("/medicalRecord")
@Validated
public class MedicalRecordController {
    private final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordMapper medicalrecordMapper;

    public MedicalRecordController(MedicalRecordService medicalRecordService, MedicalRecordMapper medicalrecordMapper) {
        this.medicalRecordService = medicalRecordService;
        this.medicalrecordMapper = medicalrecordMapper;
    }

    /**
     * Crée un nouveau dossier médical pour une personne.
     * <p>
     * Endpoint : POST /medicalRecord
     * <p>
     * Permet d'ajouter un nouveau dossier médical contenant la date de naissance,
     * les médicaments et les allergies d'une personne. Si un dossier médical existe
     * déjà pour cette personne (même prénom et nom), une exception sera levée.
     *
     * @param medicalRecordDTO le DTO contenant les informations médicales (prénom,
     *                         nom, date de naissance,
     *                         médicaments, allergies)
     * @return ResponseEntity contenant le {@link MedicalRecordDTO} créé (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(
            @Valid @RequestBody MedicalRecordDTO medicalRecordDTO) {

        logger.info("[CALL] POST /medicalRecord -> Creating medical record for {} {}",
                medicalRecordDTO.getFirstName(), medicalRecordDTO.getLastName());

        // Mapper DTO → Entity
        MedicalRecord medicalRecord = medicalrecordMapper.toEntity(medicalRecordDTO);

        // Appeler le service
        MedicalRecord createdRecord = medicalRecordService.createMedicalRecord(medicalRecord);

        // Mapper Entity → DTO pour la réponse
        MedicalRecordDTO response = medicalrecordMapper.toDto(createdRecord);

        logger.info("[RESPONSE] POST /medicalRecord -> Medical record created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour un dossier médical existant.
     * <p>
     * Endpoint : PUT /medicalRecord?firstName={firstName}&lastName={lastName}
     * <p>
     * Permet de modifier les informations médicales (date de naissance,
     * médicaments,
     * allergies) d'un dossier existant. Si le dossier n'existe pas, une exception
     * sera levée.
     *
     * @param firstName        le prénom de la personne dont on veut modifier le
     *                         dossier médical
     * @param lastName         le nom de famille de la personne dont on veut
     *                         modifier le dossier médical
     * @param medicalRecordDTO le DTO contenant les nouvelles informations médicales
     * @return ResponseEntity contenant le {@link MedicalRecordDTO} mis à jour (HTTP
     *         200)
     */
    @PutMapping
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @RequestParam("firstName") @NotBlank(message = "First name required") String firstName,
            @RequestParam("lastName") @NotBlank(message = "Last name required") String lastName,
            @RequestBody MedicalRecordDTO medicalRecordDTO) {

        logger.info("[CALL] PUT /medicalRecord -> Updating medical record for {} {}",
                firstName, lastName);

        // Mapper DTO → Entity
        MedicalRecord medicalRecord = medicalrecordMapper.toEntity(medicalRecordDTO);

        // Appeler le service
        MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(firstName, lastName, medicalRecord);

        // Mapper Entity → DTO pour la réponse
        MedicalRecordDTO response = medicalrecordMapper.toDto(updatedRecord);

        logger.info("[RESPONSE] PUT /medicalRecord -> Medical record updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un dossier médical existant.
     * <p>
     * Endpoint : DELETE /medicalRecord?firstName={firstName}&lastName={lastName}
     * <p>
     * Permet de supprimer définitivement le dossier médical d'une personne.
     * Si le dossier n'existe pas, une exception sera levée.
     *
     * @param firstName le prénom de la personne dont on veut supprimer le dossier
     *                  médical
     * @param lastName  le nom de famille de la personne dont on veut supprimer le
     *                  dossier médical
     * @return ResponseEntity sans contenu (HTTP 204) en cas de suppression réussie
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
}