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

    /* public List<MedicalRecord> findByLastName(String lastName) {
        return data.getMedicalrecords().stream()
                .filter(mr -> mr.getLastName().equals(lastName))
                .toList();
    } */
}