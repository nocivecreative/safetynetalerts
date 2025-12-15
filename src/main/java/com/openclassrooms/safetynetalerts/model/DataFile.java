package com.openclassrooms.safetynetalerts.model;

import java.util.List;

import lombok.Data;

@Data
public class DataFile {

    private List<Person> persons;
    private List<Firestation> firestations;
    private List<MedicalRecord> medicalrecords;

    public DataFile() {
    }

}
