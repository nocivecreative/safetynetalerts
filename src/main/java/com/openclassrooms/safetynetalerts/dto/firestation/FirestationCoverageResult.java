package com.openclassrooms.safetynetalerts.dto.firestation;

import java.util.List;

import com.openclassrooms.safetynetalerts.model.Person;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FirestationCoverageResult {

    private final List<Person> persons;
    private final int adultCount;
    private final int childCount;

}
