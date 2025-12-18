package com.openclassrooms.safetynetalerts.dto.floodstations;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;

@Data
public class FloodResidentDTO {

    private String lastName;
    private String phoneNumber;
    private int age;
    private MedicalHistoryDTO medicalHistory;

    public FloodResidentDTO() {
    }

    public FloodResidentDTO(String lastName, String phoneNumber, int age, MedicalHistoryDTO medicalHistory) {
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.medicalHistory = medicalHistory;
    }

}
