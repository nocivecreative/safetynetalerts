package com.openclassrooms.safetynetalerts.dto.childalert;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class HouseholdMemberDTO {

    private final String firstName;
    private final String lastName;

}
