package com.openclassrooms.safetynetalerts.model;

import java.util.List;

import lombok.Data;

@Data
public class MedicalRecord {

    private String firstName;
    private String lastName;
    private String birthdate; // TODO : Voir si typer en date est pertinent ?
    private List<String> medications;
    private List<String> allergies;

    public MedicalRecord() {
    }

}
