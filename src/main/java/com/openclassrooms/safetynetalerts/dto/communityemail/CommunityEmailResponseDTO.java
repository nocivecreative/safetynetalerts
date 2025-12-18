package com.openclassrooms.safetynetalerts.dto.communityemail;

import java.util.Set;

import lombok.Data;

@Data
public class CommunityEmailResponseDTO {

    private Set<String> emailAddresses;

    public CommunityEmailResponseDTO(Set<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

}
