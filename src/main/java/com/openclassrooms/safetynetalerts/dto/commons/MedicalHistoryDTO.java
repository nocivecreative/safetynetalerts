package com.openclassrooms.safetynetalerts.dto.commons;

import java.util.List;

import lombok.Data;

@Data
public class MedicalHistoryDTO {
    private List<String> medications; // TODO : split en medication/posology
    private List<String> allergies;

    public MedicalHistoryDTO() {
    }

    public MedicalHistoryDTO(List<String> medications, List<String> allergies) {
        this.medications = medications;
        this.allergies = allergies;
    }

}
