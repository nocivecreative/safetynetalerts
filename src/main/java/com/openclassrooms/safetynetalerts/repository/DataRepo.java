package com.openclassrooms.safetynetalerts.repository;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Person;

public interface DataRepo {
    DataFile loadData();

    boolean personExists(String firstName, String lastName);

    void addPerson(Person person);

    void updatePerson(Person person);

    void deletePerson(String firstName, String lastName);
}
