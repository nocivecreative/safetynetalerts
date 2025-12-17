package com.openclassrooms.safetynetalerts.dto;

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
