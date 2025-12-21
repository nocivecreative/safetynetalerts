package com.openclassrooms.safetynetalerts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.service.MedicalRecordService;

@RestController
@RequestMapping("/medicalRecord")
public class MedicalRecordController {
    private final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    @Autowired
    private MedicalRecordService medicalRecordService;

    @PostMapping
    public ResponseEntity<MedicalRecord> createMedicalRecord(@RequestBody MedicalRecord medicalRecord) {

        logger.info("[CALL] POST /medicalRecord - Creating medical record: {} {}",
                medicalRecord.getFirstName(), medicalRecord.getLastName());

        MedicalRecord createdRecord = medicalRecordService.createMedicalRecord(medicalRecord);

        logger.info("[RESPONSE] POST /medicalRecord - Medical record created: {} {}",
                createdRecord.getFirstName(), createdRecord.getLastName());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
    }

    @PutMapping
    public ResponseEntity<MedicalRecord> updateMedicalRecord(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestBody MedicalRecord medicalRecord) {

        logger.info("[CALL] PUT /medicalRecord?firstName={}&lastName={}", firstName, lastName);

        MedicalRecord updatedRecord = medicalRecordService.updateMedicalRecord(
                firstName, lastName, medicalRecord);

        logger.info("[RESPONSE] PUT /medicalRecord - Medical record updated: {} {}",
                updatedRecord.getFirstName(), updatedRecord.getLastName());

        return ResponseEntity.ok(updatedRecord);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMedicalRecord(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName) {

        logger.info("[CALL] DELETE /medicalRecord?firstName={}&lastName={}", firstName, lastName);

        medicalRecordService.deleteMedicalRecord(firstName, lastName);

        logger.info("[RESPONSE] DELETE /medicalRecord - Medical record deleted: {} {}",
                firstName, lastName);

        return ResponseEntity.noContent().build();
    }
}