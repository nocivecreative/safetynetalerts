package com.openclassrooms.safetynetalerts.dto;

import lombok.Data;

@Data
public class FloodPersonInfoDTO {

    private String lastName;
    private String phoneNumber;
    private int age;
    private FloodMedicalHistoryDTO medicalHistory;

    public FloodPersonInfoDTO() {
    }

    public FloodPersonInfoDTO(String lastName, String phoneNumber, int age, FloodMedicalHistoryDTO medicalHistory) {
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.medicalHistory = medicalHistory;
    }

}
