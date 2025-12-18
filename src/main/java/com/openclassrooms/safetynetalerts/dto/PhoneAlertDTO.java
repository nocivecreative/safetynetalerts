package com.openclassrooms.safetynetalerts.dto;

import java.util.Set;

import lombok.Data;

@Data
public class PhoneAlertDTO {

    private Set<String> phoneNumbersList;

    public PhoneAlertDTO() {
    }

    public PhoneAlertDTO(Set<String> phoneNumbersList) {
        this.phoneNumbersList = phoneNumbersList;
    }
}
