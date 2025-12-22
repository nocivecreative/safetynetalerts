package com.openclassrooms.safetynetalerts;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class HouseholdProfile {

    private final String address;
    private final List<PersonProfile> residents;

}
