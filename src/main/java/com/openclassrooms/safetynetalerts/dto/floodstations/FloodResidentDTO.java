package com.openclassrooms.safetynetalerts.dto.floodstations;

import java.util.Collections;
import java.util.List;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FloodResidentDTO {

    private final String lastName;
    private final String phoneNumber;
    private final int age;
    private final MedicalHistoryDTO medicalHistory;

    public List<String> getMedications() {
        return medicalHistory != null
                ? medicalHistory.getMedications()
                : Collections.emptyList();
    }

    public List<String> getAllergies() {
        return medicalHistory != null
                ? medicalHistory.getAllergies()
                : Collections.emptyList();
    }
}
