package com.openclassrooms.safetynetalerts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;

import jakarta.annotation.PostConstruct;

@Repository
public class MedicalRecordRepository {

    @Autowired
    private DataRepo dataRepo;

    private DataFile data;

    @PostConstruct
    public void init() {
        this.data = dataRepo.loadData();
    }

    public List<MedicalRecord> findAll() {
        return data.getMedicalrecords();
    }

    public Optional<MedicalRecord> findByFirstNameAndLastName(String firstName, String lastName) {
        return data.getMedicalrecords().stream()
                .filter(mr -> mr.getFirstName().equals(firstName)
                        && mr.getLastName().equals(lastName))
                .findFirst();
    }

    public boolean existsByFirstNameAndLastName(String firstName, String lastName) {
        return data.getMedicalrecords().stream()
                .anyMatch(mr -> mr.getFirstName().equals(firstName)
                        && mr.getLastName().equals(lastName));
    }

    public MedicalRecord save(MedicalRecord medicalRecord) {
        // Vérifier si le dossier existe déjà
        Optional<MedicalRecord> existing = findByFirstNameAndLastName(
                medicalRecord.getFirstName(),
                medicalRecord.getLastName());

        if (existing.isPresent()) {
            // Si il existe, on le met à jour
            MedicalRecord existingRecord = existing.get();
            updateMedicalRecordFields(existingRecord, medicalRecord);
            return existingRecord;
        } else {
            // Sinon on l'ajoute
            data.getMedicalrecords().add(medicalRecord);
            return medicalRecord;
        }
    }

    public Optional<MedicalRecord> update(String firstName, String lastName, MedicalRecord updatedRecord) {
        Optional<MedicalRecord> existing = findByFirstNameAndLastName(firstName, lastName);

        if (existing.isPresent()) {
            MedicalRecord record = existing.get();
            updateMedicalRecordFields(record, updatedRecord);
            return Optional.of(record);
        }

        return Optional.empty();
    }

    public boolean delete(String firstName, String lastName) {
        return data.getMedicalrecords().removeIf(
                mr -> mr.getFirstName().equals(firstName)
                        && mr.getLastName().equals(lastName));
    }

    private void updateMedicalRecordFields(MedicalRecord existing, MedicalRecord updated) {
        // Le prénom et nom ne changent pas (identifiant unique)
        if (updated.getBirthdate() != null) {
            existing.setBirthdate(updated.getBirthdate());
        }
        if (updated.getMedications() != null) {
            existing.setMedications(updated.getMedications());
        }
        if (updated.getAllergies() != null) {
            existing.setAllergies(updated.getAllergies());
        }
    }

}
