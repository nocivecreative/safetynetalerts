package com.openclassrooms.safetynetalerts;

import java.util.Collections;
import java.util.List;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PersonProfile {

    private final Person person;
    private final MedicalRecord medicalRecord;
    private final int age;

    public List<String> getMedications() {
        return medicalRecord != null
                ? medicalRecord.getMedications()
                : Collections.emptyList();
    }

    public List<String> getAllergies() {
        return medicalRecord != null
                ? medicalRecord.getAllergies()
                : Collections.emptyList();
    }
}