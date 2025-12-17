package com.openclassrooms.safetynetalerts.dto;

import lombok.Data;

@Data
public class PersonFireDTO {

    private String LastName;
    private String phoneNumber;
    private MedicalHistoryDTO medicalHistory;
    private int age;

    public PersonFireDTO() {
    }

    public PersonFireDTO(String lastName, String phoneNumber, MedicalHistoryDTO medicalHistory, int age) {
        LastName = lastName;
        this.phoneNumber = phoneNumber;
        this.medicalHistory = medicalHistory;
        this.age = age;
    }

}
