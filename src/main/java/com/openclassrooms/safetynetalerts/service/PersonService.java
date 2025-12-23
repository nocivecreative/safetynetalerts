package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;

@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    @Autowired
    private PersonRepository personRepository;

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

        if (personRepository.existsByFirstNameAndLastName(person.getFirstName(), person.getLastName())) {
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
        if (!personRepository.existsByFirstNameAndLastName(person.getFirstName(), person.getLastName())) {
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
        if (!personRepository.existsByFirstNameAndLastName(firstName, lastName)) {
            logger.error("[SERVICE] Person not found: {} {}",
                    firstName, lastName);
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.deletePerson(firstName, lastName);
    }

}