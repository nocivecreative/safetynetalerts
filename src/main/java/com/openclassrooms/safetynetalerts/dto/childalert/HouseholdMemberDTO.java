package com.openclassrooms.safetynetalerts.dto.childalert;

import lombok.Data;

@Data
public class HouseholdMemberDTO {

    private String firstName;
    private String lastName;

    public HouseholdMemberDTO() {
    }

    public HouseholdMemberDTO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
