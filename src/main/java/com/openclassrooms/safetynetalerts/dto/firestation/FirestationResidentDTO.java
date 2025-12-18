package com.openclassrooms.safetynetalerts.dto.firestation;

import lombok.Data;

@Data
public class FirestationResidentDTO {
    private String firstName;
    private String lastName;
    private String address;
    private String phone;

    public FirestationResidentDTO() {
    }

    public FirestationResidentDTO(String firstName, String lastName, String address, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
    }

}
