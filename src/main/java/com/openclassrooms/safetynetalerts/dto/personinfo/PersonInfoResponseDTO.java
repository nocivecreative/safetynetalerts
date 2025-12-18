package com.openclassrooms.safetynetalerts.dto.personinfo;

import java.util.List;

import lombok.Data;

@Data
public class PersonInfoResponseDTO{

    private List<PersonMedicalProfileDTO> persons;

    public PersonInfoResponseDTO(List<PersonMedicalProfileDTO> persons) {
        this.persons = persons;
    }

}