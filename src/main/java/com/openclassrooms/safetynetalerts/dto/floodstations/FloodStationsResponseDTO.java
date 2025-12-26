package com.openclassrooms.safetynetalerts.dto.floodstations;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FloodStationsResponseDTO {

    private final List<FloodStationHouseholdDTO> households;

}
