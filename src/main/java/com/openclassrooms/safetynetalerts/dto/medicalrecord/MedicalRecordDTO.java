package com.openclassrooms.safetynetalerts.dto.medicalrecord;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MedicalRecordDTO {
    @NotBlank(message = "First Name required")
    private final String firstName;
    @NotBlank(message = "Last Name required")
    private final String lastName;
    private final String birthdate;
    private final List<String> medications;
    private final List<String> allergies;

}
