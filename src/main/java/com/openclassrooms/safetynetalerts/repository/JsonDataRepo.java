package com.openclassrooms.safetynetalerts.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonDataRepo implements DataRepo {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private JsonMapper mapper;

    private DataFile dataFile;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data.json");
            this.dataFile = mapper.readValue(resource.getInputStream(), DataFile.class);
            logger.debug("[REPOSITORY] Json chargé");
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire data.json", e);
        }
    }

    @Override
    public DataFile loadData() {
        return dataFile;
    }

    @Override
    public boolean personExists(String firstName, String lastName) {
        return loadData().getPersons().stream()
                .anyMatch(p -> p.getFirstName().equals(firstName)
                        && p.getLastName().equals(lastName));
    }

    @Override
    public void addPerson(Person person) {
        loadData().getPersons().add(person);
    }

    @Override
    public void updatePerson(Person person) {
        deletePerson(person.getFirstName(), person.getLastName());
        addPerson(person);
    }

    @Override
    public void deletePerson(String firstName, String lastName) {
        loadData().getPersons().removeIf(p -> p.getFirstName().equals(firstName)
                && p.getLastName().equals(lastName));
    }

    @Override
    public void addFirestation(Firestation firestation) {
        loadData().getFirestations().add(firestation);
    }

    @Override
    public boolean firestationAddressExists(String address) {
        return loadData().getFirestations().stream()
                .anyMatch(f -> f.getAddress().equals(address));
    }

    @Override
    public void updateFirestation(Firestation firestation) {
        deleteFirestationByStation(firestation.getStation());
        addFirestation(firestation);
    }

    @Override
    public void deleteFirestationByAddress(String address) {
        loadData().getFirestations().removeIf(p -> p.getAddress().equals(address));
    }

    @Override
    public void deleteFirestationByStation(int station) {
        loadData().getFirestations().removeIf(p -> p.getStation() == (station));
    }

}
