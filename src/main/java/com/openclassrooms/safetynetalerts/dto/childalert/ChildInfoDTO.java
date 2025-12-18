package com.openclassrooms.safetynetalerts.dto.childalert;

import java.util.List;

import lombok.Data;

@Data
public class ChildInfoDTO {

    private String firstName;
    private String lastName;
    private int age;
    private List<HouseholdMemberDTO> otherHouseholdMembers;

    public ChildInfoDTO() {
    }

    public ChildInfoDTO(String firstName, String lastName, int age, List<HouseholdMemberDTO> otherHouseholdMembers) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.otherHouseholdMembers = otherHouseholdMembers;
    }

}
