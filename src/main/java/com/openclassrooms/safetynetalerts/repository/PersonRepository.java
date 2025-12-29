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

/**
 * Repository de gestion des données des personnes.
 *
 * <p>
 * Ce repository fournit l'accès aux données des personnes stockées dans le
 * fichier JSON,
 * avec les opérations suivantes :
 * <ul>
 * <li>Recherche par différents critères (adresse, nom, prénom+nom)</li>
 * <li>Récupération des emails par ville</li>
 * <li>Opérations CRUD (Create, Read, Update, Delete)</li>
 * </ul>
 *
 * <p>
 * Les personnes sont identifiées de manière unique par la combinaison prénom +
 * nom de famille.
 * Toutes les opérations de modification (add, update, delete) affectent
 * directement
 * les données en mémoire chargées depuis le fichier JSON.
 *
 * <p>
 * <b>Note sur la thread-safety :</b> Ce repository n'est pas thread-safe. Les
 * opérations
 * de modification (add, update, delete) affectent directement la liste en
 * mémoire sans
 * synchronisation. Dans un environnement multi-threadé, des mécanismes de
 * synchronisation
 * externes doivent être mis en place au niveau de la couche service.
 *
 */
@Repository
public class PersonRepository {

    @Autowired
    private DataRepo dataRepo;

    private DataFile data;

    /**
     * Initialise le repository en chargeant les données depuis le fichier JSON.
     *
     * <p>
     * Cette méthode est appelée automatiquement après la construction du bean
     * Spring
     * grâce à l'annotation {@link PostConstruct}. Elle charge toutes les données
     * en mémoire pour un accès rapide.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est appelée une seule fois au démarrage
     * de l'application par le conteneur Spring, avant que le bean ne soit utilisé.
     */
    @PostConstruct
    public void init() {
        this.data = dataRepo.loadData();
    }

    /**
     * Récupère la liste complète de toutes les personnes.
     *
     * <p>
     * Cette méthode retourne la liste complète sans aucun filtre.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode retourne la référence directe à la liste
     * en mémoire. Elle n'est pas thread-safe si des modifications concurrentes sont
     * effectuées.
     *
     * @return la liste de toutes les personnes, ou une liste vide si aucune
     *         personne n'est enregistrée
     */
    public List<Person> findAll() {
        return data.getPersons();
    }

    /**
     * Recherche toutes les personnes habitant à une adresse donnée.
     *
     * <p>
     * Cette méthode effectue une correspondance exacte sur l'adresse.
     * Elle est utilisée notamment pour récupérer tous les résidents d'un foyer.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'une nouvelle liste via {@code toList()} garantit l'isolation
     * des données retournées.
     *
     * @param address l'adresse à rechercher (sensible à la casse, correspondance
     *                exacte)
     * @return la liste des personnes habitant à cette adresse, ou une liste vide si
     *         aucune correspondance
     */
    public List<Person> findByAddress(String address) {
        return data.getPersons().stream()
                .filter(p -> p.getAddress().equals(address))
                .toList();
    }

    /**
     * Recherche une personne par son prénom et son nom de famille.
     *
     * <p>
     * Cette méthode effectue une correspondance exacte sur la combinaison prénom +
     * nom,
     * qui constitue l'identifiant unique d'une personne dans le système.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param firstName le prénom à rechercher (sensible à la casse, correspondance
     *                  exacte)
     * @param lastName  le nom de famille à rechercher (sensible à la casse,
     *                  correspondance exacte)
     * @return un {@link Optional} contenant la personne si trouvée, sinon
     *         {@link Optional#empty()}
     */
    public Optional<Person> findByFirstNameAndLastName(String firstName, String lastName) {
        return data.getPersons().stream()
                .filter(p -> p.getFirstName().equals(firstName)
                        && p.getLastName().equals(lastName))
                .findFirst();
    }

    /**
     * Recherche toutes les personnes portant un nom de famille donné.
     *
     * <p>
     * Cette méthode est utile pour retrouver tous les membres d'une même famille.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'une nouvelle liste via {@code toList()} garantit l'isolation
     * des données retournées.
     *
     * @param lastName le nom de famille à rechercher (sensible à la casse,
     *                 correspondance exacte)
     * @return la liste des personnes portant ce nom, ou une liste vide si aucune
     *         correspondance
     */
    public List<Person> findByLastName(String lastName) {
        return data.getPersons().stream()
                .filter(mr -> mr.getLastName().equals(lastName))
                .toList();
    }

    /*
     * public List<Person> findByCity(String city
     * return data.getPersons().stream()
     * .filter(p -> p.getCity().equals(city))
     * .toList();
     * }
     */

    /**
     * Récupère l'ensemble des adresses email uniques des résidents d'une ville.
     *
     * <p>
     * Cette méthode retourne un {@link Set} garantissant l'unicité des emails.
     * Elle est utilisée pour envoyer des alertes par email à tous les résidents
     * d'une ville.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'un nouveau Set via {@code collect()} garantit l'isolation
     * des données retournées.
     *
     * @param city le nom de la ville (sensible à la casse, correspondance exacte)
     * @return un ensemble d'adresses email uniques, ou un ensemble vide si aucun
     *         résident trouvé
     */
    public Set<String> findEmailsByCity(String city) {
        return data.getPersons().stream()
                .filter(p -> p.getCity().equals(city))
                .map(Person::getEmail)
                .collect(Collectors.toSet());
    }

    /**
     * Vérifie si une personne existe dans le système.
     *
     * <p>
     * Cette méthode vérifie l'existence d'une personne par la combinaison unique
     * prénom + nom de famille.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param firstName le prénom de la personne à rechercher
     * @param lastName  le nom de famille de la personne à rechercher
     * @return {@code true} si la personne existe, {@code false} sinon
     */
    public boolean existsByFirstNameAndLastName(String firstName, String lastName) {
        return data.getPersons().stream()
                .anyMatch(p -> p.getFirstName().equals(firstName)
                        && p.getLastName().equals(lastName));
    }

    /**
     * Ajoute une nouvelle personne au système.
     *
     * <p>
     * Cette méthode ajoute directement la personne à la liste en mémoire.
     * Aucune validation d'unicité n'est effectuée à ce niveau - c'est la
     * responsabilité
     * du service appelant de vérifier que la personne n'existe pas déjà.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param person la personne à ajouter
     */
    public void addPerson(Person person) {
        data.getPersons().add(person);
    }

    /**
     * Met à jour les informations d'une personne existante.
     *
     * <p>
     * Cette méthode met à jour uniquement les champs modifiables de la personne :
     * address, city, zip, phone et email. L'identité de la personne (prénom + nom)
     * ne peut pas être modifiée conformément au cahier des charges.
     *
     * <p>
     * Si aucune personne ne correspond à la combinaison prénom + nom,
     * aucune modification n'est effectuée.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement l'objet en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param person les nouvelles données de la personne (prénom et nom servent
     *               d'identifiant)
     */
    public void updatePerson(Person person) {
        findByFirstNameAndLastName(person.getFirstName(), person.getLastName())
                .ifPresent(existingPerson -> {
                    existingPerson.setAddress(person.getAddress());
                    existingPerson.setCity(person.getCity());
                    existingPerson.setZip(person.getZip());
                    existingPerson.setPhone(person.getPhone());
                    existingPerson.setEmail(person.getEmail());
                });
    }

    /**
     * Supprime une personne du système.
     *
     * <p>
     * Cette méthode supprime la personne identifiée par la combinaison prénom + nom
     * de famille.
     * Si aucune personne ne correspond, aucune erreur n'est levée (suppression
     * idempotente).
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param firstName le prénom de la personne à supprimer
     * @param lastName  le nom de famille de la personne à supprimer
     */
    public void deletePerson(String firstName, String lastName) {
        data.getPersons().removeIf(p -> p.getFirstName().equals(firstName)
                && p.getLastName().equals(lastName));
    }
}
