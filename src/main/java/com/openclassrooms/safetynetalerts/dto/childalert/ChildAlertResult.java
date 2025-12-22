package com.openclassrooms.safetynetalerts.dto.childalert;

import java.util.List;

import com.openclassrooms.safetynetalerts.model.Person;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ChildAlertResult {

    private final List<Person> children;
    private final List<Person> adults;

}
