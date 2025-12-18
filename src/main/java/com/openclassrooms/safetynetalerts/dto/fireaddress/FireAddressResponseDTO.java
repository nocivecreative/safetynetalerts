package com.openclassrooms.safetynetalerts.dto.fireaddress;

import java.util.List;

import lombok.Data;

@Data
public class FireAddressResponseDTO {
    private List<FireAddressResidentDTO> personList;
    private String firestationNumber;

    public FireAddressResponseDTO(List<FireAddressResidentDTO> personList, String firestationNumber) {
        this.personList = personList;
        this.firestationNumber = firestationNumber;
    }
}
