package com.openclassrooms.safetynetalerts.dto;

import java.util.List;

import lombok.Data;

@Data
public class FloodMedicalHistoryDTO {
    private List<String> medications; // TODO : split en medication/posology
    private List<String> allergies;

    public FloodMedicalHistoryDTO() {
    }

    public FloodMedicalHistoryDTO(List<String> medications, List<String> allergies) {
        this.medications = medications;
        this.allergies = allergies;
    }

}
