package com.openclassrooms.safetynetalerts.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;

@Service
public class MedicalRecordService {
    private final Logger logger = LoggerFactory.getLogger(MedicalRecordService.class);

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        logger.info("Creating medical record: {} {}",
                medicalRecord.getFirstName(), medicalRecord.getLastName());

        // Vérifier si le dossier existe déjà
        if (medicalRecordRepository.existsByFirstNameAndLastName(
                medicalRecord.getFirstName(),
                medicalRecord.getLastName())) {
            logger.error("Medical record already exists: {} {}",
                    medicalRecord.getFirstName(), medicalRecord.getLastName());
            throw new IllegalArgumentException(
                    "Medical record for " + medicalRecord.getFirstName() + " "
                            + medicalRecord.getLastName() + " already exists");
        }

        // Sauvegarder
        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        logger.info("Medical record created successfully: {} {}",
                savedRecord.getFirstName(), savedRecord.getLastName());

        return savedRecord;
    }

    public MedicalRecord updateMedicalRecord(String firstName, String lastName,
            MedicalRecord medicalRecord) {
        logger.info("Updating medical record: {} {}", firstName, lastName);

        // Mettre à jour
        Optional<MedicalRecord> updatedRecord = medicalRecordRepository.update(
                firstName, lastName, medicalRecord);

        if (updatedRecord.isEmpty()) {
            logger.warn("Medical record not found: {} {}", firstName, lastName);
            throw new IllegalArgumentException(
                    "Medical record for " + firstName + " " + lastName + " not found");
        }

        logger.info("Medical record updated successfully: {} {}", firstName, lastName);
        return updatedRecord.get();
    }

    public void deleteMedicalRecord(String firstName, String lastName) {
        logger.info("Deleting medical record: {} {}", firstName, lastName);

        boolean deleted = medicalRecordRepository.delete(firstName, lastName);

        if (!deleted) {
            logger.warn("Medical record not found: {} {}", firstName, lastName);
            throw new IllegalArgumentException(
                    "Medical record for " + firstName + " " + lastName + " not found");
        }

        logger.info("Medical record deleted successfully: {} {}", firstName, lastName);
    }
}