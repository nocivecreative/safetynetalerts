package com.openclassrooms.safetynetalerts.dto.fireaddress;

import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FireAddressResidentDTO {

    private final String LastName;
    private final String phoneNumber;
    private final MedicalHistoryDTO medicalHistory;
    private final int age;

}
