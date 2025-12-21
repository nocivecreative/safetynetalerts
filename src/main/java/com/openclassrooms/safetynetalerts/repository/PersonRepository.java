package com.openclassrooms.safetynetalerts.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;

import com.openclassrooms.safetynetalerts.model.Person;

import jakarta.annotation.PostConstruct;

@Repository
public class PersonRepository {

    @Autowired
    private DataRepo dataRepo;

    private DataFile data;

    @PostConstruct
    public void init() {
        this.data = dataRepo.loadData();
    }

    public List<Person> findAll() {
        return data.getPersons();
    }

    public List<Person> findByAddress(String address) {
        return data.getPersons().stream()
                .filter(p -> p.getAddress().equals(address))
                .toList();
    }

    public Optional<Person> findByFirstNameAndLastName(String firstName, String lastName) {
        return data.getPersons().stream()
                .filter(p -> p.getFirstName().equals(firstName)
                        && p.getLastName().equals(lastName))
                .findFirst();
    }

    public List<Person> findByLastName(String lastName) {
        return data.getPersons().stream()
                .filter(mr -> mr.getLastName().equals(lastName))
                .toList();
    }

    /* public List<Person> findByCity(String city
        return data.getPersons().stream()
                .filter(p -> p.getCity().equals(city))
                .toList();
    } */

    public Set<String> findEmailsByCity(String city) {
        return data.getPersons().stream()
                .filter(p -> p.getCity().equals(city))
                .map(Person::getEmail)
                .collect(Collectors.toSet());
    }

    public boolean personExists(String firstName, String lastName) {
        return data.getPersons().stream()
                .anyMatch(p -> p.getFirstName().equals(firstName)
                        && p.getLastName().equals(lastName));
    }

    public void addPerson(Person person) {
        data.getPersons().add(person);
    }

    public void updatePerson(Person person) {
        deletePerson(person.getFirstName(), person.getLastName());
        addPerson(person);
    }

    public void deletePerson(String firstName, String lastName) {
        data.getPersons().removeIf(p -> p.getFirstName().equals(firstName)
                && p.getLastName().equals(lastName));
    }
}
