package com.openclassrooms.safetynetalerts.dto.floodstations;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FloodStationHouseholdDTO {

    private final String address;
    private final List<FloodResidentDTO> residents;

}
