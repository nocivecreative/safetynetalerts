package com.openclassrooms.safetynetalerts.dto.firestation;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FirestationResidentDTO {
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String phone;

}
