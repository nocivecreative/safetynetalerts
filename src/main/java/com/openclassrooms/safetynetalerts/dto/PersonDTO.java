package com.openclassrooms.safetynetalerts.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PersonDTO {

    private final String firstName;
    private final String lastName;
    private final String address;
    private final String city;
    private final String zip;
    private final String phone;
    private final String email;
}
