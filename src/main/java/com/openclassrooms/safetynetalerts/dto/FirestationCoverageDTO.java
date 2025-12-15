package com.openclassrooms.safetynetalerts.dto;

import java.util.List;

import lombok.Data;

@Data
public class FirestationCoverageDTO {
    private List<PersonInfoDTO> persons;
    private int adultCount;
    private int childCount;

    public FirestationCoverageDTO() {
    }

    public FirestationCoverageDTO(List<PersonInfoDTO> persons, int adultCount, int childCount) {
        this.persons = persons;
        this.adultCount = adultCount;
        this.childCount = childCount;
    }

}
