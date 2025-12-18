package com.openclassrooms.safetynetalerts.dto;

import lombok.Data;

@Data
public class PersonInfolastNameDTO {

    private String lastname;
    private String address;
    private int age;
    private String email;
    private PersonInfolastNameMedicalHistoryDTO medicalHistory;

    public PersonInfolastNameDTO(String lastname, String address, int age, String email,
            PersonInfolastNameMedicalHistoryDTO medicalHistory) {
        this.lastname = lastname;
        this.address = address;
        this.age = age;
        this.email = email;
        this.medicalHistory = medicalHistory;
    }

}
