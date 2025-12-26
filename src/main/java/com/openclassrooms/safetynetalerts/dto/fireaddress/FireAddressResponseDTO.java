package com.openclassrooms.safetynetalerts.dto.fireaddress;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FireAddressResponseDTO {
    private final List<FireAddressResidentDTO> personList;
    private final int firestationNumber;

}
