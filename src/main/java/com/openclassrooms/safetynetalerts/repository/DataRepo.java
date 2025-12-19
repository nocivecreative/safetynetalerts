package com.openclassrooms.safetynetalerts.repository;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;

public interface DataRepo {
    DataFile loadData();

    boolean personExists(String firstName, String lastName);

    void addPerson(Person person);

    void updatePerson(Person person);

    void deletePerson(String firstName, String lastName);

    boolean firestationAddressExists(String address);

    void addFirestation(Firestation firestation);

    void updateFirestation(Firestation firestation);

    void deleteFirestationByAddress(String address);

    void deleteFirestationByStation(int station);
}
