package com.openclassrooms.safetynetalerts.dto;

import java.util.List;

import lombok.Data;

@Data
public class FloodDTO {

    private String address;
    private List<FloodPersonInfoDTO> persons;

    public FloodDTO() {
    }

    public FloodDTO(String address, List<FloodPersonInfoDTO> persons) {
        this.address = address;
        this.persons = persons;
    }

}
