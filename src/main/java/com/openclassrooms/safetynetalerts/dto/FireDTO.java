package com.openclassrooms.safetynetalerts.dto;

import java.util.List;

import lombok.Data;

@Data
public class FireDTO {
    private List<PersonFireDTO> personList;
    private String firestationNumber;

    public FireDTO(List<PersonFireDTO> personList, String firestationNumber) {
        this.personList = personList;
        this.firestationNumber = firestationNumber;
    }
}
