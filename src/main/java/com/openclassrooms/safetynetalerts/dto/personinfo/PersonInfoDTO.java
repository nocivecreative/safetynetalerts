package com.openclassrooms.safetynetalerts.dto.personinfo;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PersonInfoDTO {
    private String lastName;
    private String address;
    private int age;
    private String email;
    private MedicalHistoryDTO medicalHistory;
}
