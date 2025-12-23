package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    // @Autowired
    // private DataRepo dataRepo;

    @Autowired
    private FirestationRepository firestationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private Utils utils;

    /**
     * Récupère toutes les personnes vivant à une adresse donnée
     * 
     * @param address L'adresse à rechercher
     * @return Liste des personnes à cette adresse
     */
    public List<Person> getPersonsByAddress(String address) {
        logger.debug("[SERVICE] Recherche des personnes vivant à l'adresse={}", address);
        return personRepository.findByAddress(address);
    }

    public Person getPersonsByStations(List<Integer> stations) {
        logger.debug("[SERVICE] looking for perons covered by stations={}", stations);

        // Récupére toutes les adresses couvertes par les stations
        Set<String> addresses = firestationRepository.findAddressesByStations(stations);

        List<List<Person>> persons = new ArrayList<>();
        // Pour chaque adresse, créer une liste des personnes
        for (String address : addresses) {

            persons.add(personRepository.findByAddress(address));

        }

        return null;
    }

    public List<Person> getPersonsByLastName(String lastName) {
        return personRepository.findByLastName(lastName);
    }

    public Set<String> getEmailsByCity(String city) {
        return personRepository.findEmailsByCity(city);
    }

    /**
     * Ajoute une nouvelle personne
     * 
     * @param person La personne à ajouter
     * @throws IllegalArgumentException si la personne existe déjà
     */
    public void addPerson(Person person) {

        if (personRepository.personExists(person.getFirstName(), person.getLastName())) {
            logger.error("[SERVICE] Person already exist: {} {}",
                    person.getFirstName(), person.getLastName());
            throw new IllegalArgumentException("Person already exist");
        }

        personRepository.addPerson(person);
    }

    /**
     * Met à jour une personne existante
     * 
     * @param person Les nouvelles données de la personne
     * @throws IllegalArgumentException si la personne n'existe pas
     */
    public void updatePerson(Person person) {
        if (!personRepository.personExists(person.getFirstName(), person.getLastName())) {
            logger.error("[SERVICE] Person not found: {} {}",
                    person.getFirstName(), person.getLastName());
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.updatePerson(person);
    }

    /**
     * Supprime une personne
     * 
     * @param firstName Prénom
     * @param lastName  Nom
     * @throws IllegalArgumentException si la personne n'existe pas
     */
    public void deletePerson(String firstName, String lastName) {
        if (!personRepository.personExists(firstName, lastName)) {
            logger.error("[SERVICE] Person not found: {} {}",
                    firstName, lastName);
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.deletePerson(firstName, lastName);
    }

}