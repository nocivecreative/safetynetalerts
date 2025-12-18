package com.openclassrooms.safetynetalerts.dto.fireaddress;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;

@Data
public class FireAddressResidentDTO {

    private String LastName;
    private String phoneNumber;
    private int age;
    private MedicalHistoryDTO medicalHistory;

    public FireAddressResidentDTO() {
    }

    public FireAddressResidentDTO(String lastName, String phoneNumber, MedicalHistoryDTO medicalHistory, int age) {
        LastName = lastName;
        this.phoneNumber = phoneNumber;
        this.medicalHistory = medicalHistory;
        this.age = age;
    }

}
