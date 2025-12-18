package com.openclassrooms.safetynetalerts.dto.childalert;

import java.util.List;

import lombok.Data;

@Data
public class ChildAlertResponseDTO {
    private List<ChildInfoDTO> children;

    public ChildAlertResponseDTO() {
    };

    public ChildAlertResponseDTO(List<ChildInfoDTO> children) {
        this.children = children;
    }
}
