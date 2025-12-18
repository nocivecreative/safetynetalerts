package com.openclassrooms.safetynetalerts.dto.floodstations;

import java.util.List;

import lombok.Data;

@Data
public class FloodStationsResponseDTO {

    private String address;
    private List<FloodResidentDTO> residents;

    public FloodStationsResponseDTO() {
    }

    public FloodStationsResponseDTO(String address, List<FloodResidentDTO> residents) {
        this.address = address;
        this.residents = residents;
    }

}
