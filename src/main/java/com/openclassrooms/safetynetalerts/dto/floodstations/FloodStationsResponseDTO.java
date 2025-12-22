package com.openclassrooms.safetynetalerts.dto.floodstations;

import java.util.List;

import lombok.Data;

@Data
public class FloodStationsResponseDTO {

    private List<FloodStationHouseholdDTO> households;

    public FloodStationsResponseDTO(List<FloodStationHouseholdDTO> households) {
        this.households = households;
    }

}
