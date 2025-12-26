package com.openclassrooms.safetynetalerts.dto.commons;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MedicalHistoryDTO {
    private final List<String> medications; // TODO : split en medication/posology
    private final List<String> allergies;
}
