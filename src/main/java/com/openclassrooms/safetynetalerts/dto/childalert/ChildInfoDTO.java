package com.openclassrooms.safetynetalerts.dto.childalert;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ChildInfoDTO {

    private final String firstName;
    private final String lastName;
    private final int age;
    private final List<HouseholdMemberDTO> otherHouseholdMembers;

}
