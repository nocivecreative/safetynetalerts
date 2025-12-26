package com.openclassrooms.safetynetalerts.dto.personinfo;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PersonMedicalProfileDTO {

    private final String lastName;
    private final String address;
    private final int age;
    private final String email;
    private final MedicalHistoryDTO medicalHistory;

}
