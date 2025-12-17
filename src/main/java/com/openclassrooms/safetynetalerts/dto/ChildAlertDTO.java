package com.openclassrooms.safetynetalerts.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChildAlertDTO {
    private List<ChildInfoDTO> children;
    private List<HouseholdMemberDTO> householdMembers;

    public ChildAlertDTO() {
    };

    public ChildAlertDTO(List<ChildInfoDTO> children, List<HouseholdMemberDTO> householdMembers) {
        this.children = children;
        this.householdMembers = householdMembers;
    }
}
