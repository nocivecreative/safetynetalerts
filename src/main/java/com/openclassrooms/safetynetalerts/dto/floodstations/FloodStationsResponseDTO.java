package com.openclassrooms.safetynetalerts.dto.floodstations;

import java.util.List;

import lombok.Data;

@Data
public class FloodStationsResponseDTO {

    private List<FloodHouseholdDTO> households;

    public FloodStationsResponseDTO(List<FloodHouseholdDTO> households) {
        this.households = households;
    }

}
