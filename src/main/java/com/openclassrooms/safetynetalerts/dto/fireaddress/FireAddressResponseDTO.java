package com.openclassrooms.safetynetalerts.dto.fireaddress;

import java.util.List;

import lombok.Data;

@Data
public class FireAddressResponseDTO {
    private List<FireAddressResidentDTO> personList;
    private int firestationNumber;

    public FireAddressResponseDTO(List<FireAddressResidentDTO> personList, int firestationNumber) {
        this.personList = personList;
        this.firestationNumber = firestationNumber;
    }
}
