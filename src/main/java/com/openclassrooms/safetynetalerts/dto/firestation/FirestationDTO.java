package com.openclassrooms.safetynetalerts.dto.firestation;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FirestationDTO {

    private final String address;
    private final Integer station;

}
