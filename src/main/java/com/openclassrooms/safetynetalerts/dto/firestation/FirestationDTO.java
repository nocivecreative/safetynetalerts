package com.openclassrooms.safetynetalerts.dto.firestation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FirestationDTO {
    @NotBlank(message = "Address is required")
    private final String address;
    @NotNull(message = "Station number is required")
    private final Integer station;

}
