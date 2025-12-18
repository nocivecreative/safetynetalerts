package com.openclassrooms.safetynetalerts.dto.personinfo;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;

@Data
public class PersonInfoResponseDTO {

    private String lastname;
    private String address;
    private int age;
    private String email;
    private MedicalHistoryDTO medicalHistory;

    public PersonInfoResponseDTO(String lastname, String address, int age, String email,
            MedicalHistoryDTO medicalHistory) {
        this.lastname = lastname;
        this.address = address;
        this.age = age;
        this.email = email;
        this.medicalHistory = medicalHistory;
    }

}
