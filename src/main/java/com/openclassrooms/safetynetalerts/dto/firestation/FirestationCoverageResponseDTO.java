package com.openclassrooms.safetynetalerts.dto.firestation;

import java.util.List;

import lombok.Data;

@Data
public class FirestationCoverageResponseDTO {
    private List<FirestationResidentDTO> residents;
    private int adultCount;
    private int childCount;

    public FirestationCoverageResponseDTO() {
    }

    public FirestationCoverageResponseDTO(List<FirestationResidentDTO> residents, int adultCount, int childCount) {
        this.residents = residents;
        this.adultCount = adultCount;
        this.childCount = childCount;
    }

}
