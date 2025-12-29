package com.openclassrooms.safetynetalerts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PersonDTO {

    @NotBlank(message = "First Name required")
    private final String firstName;
    @NotBlank(message = "Last Name required")
    private final String lastName;
    private final String address;
    private final String city;
    private final String zip;
    private final String phone;
    private final String email;
}
