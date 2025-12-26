package com.openclassrooms.safetynetalerts.dto.childalert;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ChildAlertResponseDTO {
    private final List<ChildInfoDTO> children;
}
