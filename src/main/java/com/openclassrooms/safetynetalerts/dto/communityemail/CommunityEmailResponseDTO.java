package com.openclassrooms.safetynetalerts.dto.communityemail;

import java.util.Set;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CommunityEmailResponseDTO {

    private final Set<String> emailAddresses;

}
