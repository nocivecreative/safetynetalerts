package com.openclassrooms.safetynetalerts.dto.personinfo;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PersonInfoResponseDTO {

    private final List<PersonMedicalProfileDTO> persons;

}