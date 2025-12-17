package com.openclassrooms.safetynetalerts.dto;

import java.util.List;

import lombok.Data;

@Data
public class PhoneAlertDTO {

    private List<String> phoneNumbersList;

    public PhoneAlertDTO() {
    }

    public PhoneAlertDTO(List<String> phoneNumbersList) {
        this.phoneNumbersList = phoneNumbersList;
    }
}
