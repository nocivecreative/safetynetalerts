package com.openclassrooms.safetynetalerts.dto.medicalrecord;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MedicalRecordDTO {

    private final String firstName;
    private final String lastName;
    private final String birthdate;
    private final List<String> medications;
    private final List<String> allergies;

}
