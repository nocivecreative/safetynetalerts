package com.openclassrooms.safetynetalerts.dto.firestation;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FirestationCoverageResponseDTO {
    private final List<FirestationResidentDTO> residents;
    private final int adultCount;
    private final int childCount;

}
