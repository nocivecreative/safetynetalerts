package com.openclassrooms.safetynetalerts.dto.phonealert;

import java.util.Set;

import lombok.Data;

@Data
public class PhoneAlertResponseDTO {

    private Set<String> phoneNumbersList;

    public PhoneAlertResponseDTO() {
    }

    public PhoneAlertResponseDTO(Set<String> phoneNumbersList) {
        this.phoneNumbersList = phoneNumbersList;
    }
}
