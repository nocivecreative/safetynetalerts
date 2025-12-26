package com.openclassrooms.safetynetalerts.dto.phonealert;

import java.util.Set;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PhoneAlertResponseDTO {

    private final Set<String> phoneNumbersList;

}
