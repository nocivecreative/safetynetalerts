package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;

/**
 * Service de gestion des personnes.
 *
 * <p>
 * Ce service fournit les opérations métier liées aux personnes, incluant :
 * <ul>
 * <li>Gestion CRUD des personnes</li>
 * <li>Recherche par adresse, nom de famille ou ville</li>
 * <li>Récupération des emails par ville</li>
 * </ul>
 *
 * @author SafetyNet Alerts
 * @version 1.0
 */
@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Récupère toutes les personnes vivant à une adresse donnée.
     *
     * <p>
     * Cette méthode retourne la liste complète des personnes habitant à l'adresse
     * spécifiée.
     *
     * @param address l'adresse à rechercher (doit correspondre exactement)
     * @return la liste des personnes habitant à cette adresse, ou une liste vide si
     *         aucune personne n'est trouvée
     */
    public List<Person> getPersonsByAddress(String address) {
        logger.debug("[SERVICE] Recherche des personnes vivant à l'adresse={}", address);
        return personRepository.findByAddress(address);
    }

    /**
     * Recherche toutes les personnes portant un nom de famille donné.
     *
     * <p>
     * Cette méthode est utile pour retrouver tous les membres d'une même famille.
     *
     * @param lastName le nom de famille à rechercher (sensible à la casse)
     * @return la liste des personnes portant ce nom, ou une liste vide si aucune
     *         personne n'est trouvée
     */
    public List<Person> getPersonsByLastName(String lastName) {
        return personRepository.findByLastName(lastName);
    }

    /**
     * Récupère l'ensemble des adresses email uniques des résidents d'une ville.
     *
     * <p>
     * Cette méthode retourne un {@link Set} garantissant l'unicité des emails.
     * Elle est utilisée pour envoyer des alertes par email à tous les résidents
     * d'une ville.
     *
     * @param city le nom de la ville (sensible à la casse)
     * @return un ensemble d'adresses email uniques, ou un ensemble vide si aucun
     *         résident n'est trouvé
     */
    public Set<String> getEmailsByCity(String city) {
        return personRepository.findEmailsByCity(city);
    }

    /**
     * Ajoute une nouvelle personne dans le système.
     *
     * <p>
     * Cette méthode vérifie d'abord que la personne n'existe pas déjà
     * (basé sur la combinaison prénom + nom de famille) avant de l'ajouter.
     * Si une personne avec le même prénom et nom existe déjà, une exception est
     * levée.
     *
     * <p>
     * L'identité d'une personne est déterminée par la combinaison unique
     * de son prénom et de son nom de famille.
     *
     * @param person la personne à ajouter (doit contenir au minimum firstName et
     *               lastName)
     * @throws IllegalArgumentException si une personne avec le même prénom et nom
     *                                  existe déjà
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
     * Met à jour les informations d'une personne existante.
     *
     * <p>
     * Cette méthode recherche la personne par la combinaison prénom + nom de
     * famille,
     * puis met à jour toutes ses informations (adresse, ville, code postal,
     * téléphone, email).
     *
     * <p>
     * L'identité de la personne (prénom et nom) ne peut pas être modifiée.
     * Pour changer l'identité, il faut supprimer la personne et en créer une
     * nouvelle.
     *
     * <p>
     * Si la personne n'existe pas dans le système, une exception est levée.
     *
     * @param person les nouvelles données de la personne (firstName et lastName
     *               servent d'identifiant)
     * @throws IllegalArgumentException si la personne n'existe pas dans le système
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
     * Supprime une personne du système.
     *
     * <p>
     * Cette méthode recherche et supprime la personne identifiée par
     * la combinaison prénom + nom de famille.
     *
     * <p>
     * <strong>Attention :</strong> Cette opération ne supprime PAS automatiquement
     * le dossier médical associé à cette personne. Il est recommandé de supprimer
     * le dossier médical séparément via le service MedicalRecordService si
     * nécessaire.
     *
     * <p>
     * Si la personne n'existe pas dans le système, une exception est levée.
     *
     * @param firstName le prénom de la personne à supprimer
     * @param lastName  le nom de famille de la personne à supprimer
     * @throws IllegalArgumentException si la personne n'existe pas dans le système
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