package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;

/**
 * Service de gestion des casernes de pompiers et de leurs zones de couverture.
 *
 * <p>
 * Ce service fournit les opérations métier liées aux casernes, incluant :
 * <ul>
 * <li>Gestion CRUD des mappings caserne/adresse</li>
 * <li>Récupération des personnes couvertes par une caserne</li>
 * <li>Récupération des numéros de téléphone pour les alertes</li>
 * </ul>
 *
 * <p>
 * Le système de mapping caserne/adresse permet d'associer plusieurs adresses
 * à une même caserne, définissant ainsi les zones de couverture géographique
 * pour les interventions d'urgence.
 *
 * @author SafetyNet Alerts
 * @version 1.0
 */
@Service
public class FirestationService {
    private final Logger logger = LoggerFactory.getLogger(FirestationService.class);

    private final FirestationRepository firestationRepository;
    private final PersonRepository personRepository;

    public FirestationService(FirestationRepository firestationRepository, PersonRepository personRepository) {
        this.firestationRepository = firestationRepository;
        this.personRepository = personRepository;
    }

    /**
     * Récupère la liste de toutes les personnes couvertes par une caserne donnée.
     *
     * <p>
     * Cette méthode retourne toutes les personnes habitant dans la zone de
     * couverture
     * de la caserne spécifiée.
     *
     * @param stationNumber le numéro de la caserne à interroger
     * @return la liste des personnes couvertes par cette caserne, ou une liste vide
     *         si aucune personne n'est trouvée
     */
    public List<Person> getPersonsCoveredByStation(int stationNumber) {
        logger.debug("[SERVICE] Recherche des personnes couvertes par la station={}", stationNumber);

        List<String> addresses = firestationRepository.findAddressesByStation(stationNumber);

        List<Person> persons = addresses.stream()
                .flatMap(address -> personRepository.findByAddress(address).stream())
                .toList();

        return persons;
    }

    /**
     * Récupère le numéro de station pour une adresse donnée.
     *
     * @param address l'adresse à rechercher
     * @return le numéro de station, ou -1 si non trouvé
     */
    public Integer getStationNumberByAddress(String address) {
        logger.debug("[SERVICE] Recherche du numéro de station pour l'adresse={}", address);
        return firestationRepository.findStationNumberByAddress(address).orElse(-1);
    }

    /**
     * Récupère l'ensemble des adresses couvertes par plusieurs stations.
     *
     * @param stations la liste des numéros de stations
     * @return l'ensemble des adresses couvertes
     */
    public Set<String> getAddressesByStations(List<Integer> stations) {
        logger.debug("[SERVICE] Recherche des adresses pour les stations={}", stations);
        return firestationRepository.findAddressesByStations(stations);
    }

    /**
     * Récupère l'ensemble des numéros de téléphone uniques des personnes couvertes
     * par une caserne.
     *
     * <p>
     * Cette méthode est utilisée pour déclencher des alertes téléphoniques en cas
     * d'urgence.
     * Elle retourne un ensemble (Set) de numéros de téléphone uniques, éliminant
     * automatiquement
     * les doublons si plusieurs personnes d'un même foyer ont le même numéro.
     *
     * <p>
     * Les numéros sont automatiquement triés dans l'ordre alphabétique grâce à
     * l'utilisation
     * d'un TreeSet.
     *
     * @param stationNumber le numéro de la caserne dont on veut les numéros de
     *                      téléphone
     * @return un ensemble de numéros de téléphone uniques, triés alphabétiquement
     */
    public Set<String> getPhoneNumbersByStation(int stationNumber) {
        logger.debug("[SERVICE] Recherche des téléphones pour la station={}", stationNumber);

        List<String> addresses = firestationRepository.findAddressesByStation(stationNumber);

        return addresses.stream()
                .flatMap(address -> personRepository.findByAddress(address).stream())
                .map(Person::getPhone)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Ajoute un nouveau mapping entre un numéro de caserne et une adresse.
     *
     * <p>
     * Cette méthode permet d'associer une nouvelle adresse à une caserne de
     * pompiers,
     * définissant ainsi la zone de couverture pour les interventions d'urgence.
     *
     * <p>
     * Les règles de validation suivantes sont appliquées :
     * <ul>
     * <li>L'adresse ne doit pas déjà être couverte par une autre caserne</li>
     * <li>Le numéro de caserne ne doit pas déjà exister dans le système</li>
     * </ul>
     *
     * <p>
     * Si l'une de ces conditions n'est pas respectée, une exception est levée.
     *
     * @param firestation le mapping caserne/adresse à ajouter
     * @throws IllegalArgumentException si l'adresse existe déjà ou si le numéro de
     *                                  caserne existe déjà
     */
    public void addMapping(Firestation firestation) {
        if (firestationRepository.existsByAddress(firestation.getAddress())) {
            throw new IllegalArgumentException("L'adresse existe déjà");
        }
        if (firestationRepository.existsByStation(firestation.getStation())) {
            throw new IllegalArgumentException("Le numéro de la caserne existe déjà");
        }
        firestationRepository.addFirestation(firestation);
    }

    /**
     * Met à jour le mapping entre un numéro de caserne et une adresse existante.
     *
     * <p>
     * Cette méthode permet de modifier le numéro de caserne associé à une adresse
     * donnée.
     * Par exemple, si une adresse change de zone de couverture et dépend désormais
     * d'une
     * autre caserne.
     *
     * <p>
     * L'adresse sert d'identifiant pour la mise à jour. Si l'adresse n'existe pas
     * dans le système, une exception est levée.
     *
     * @param firestation le mapping caserne/adresse mis à jour (l'adresse sert
     *                    d'identifiant)
     * @throws IllegalArgumentException si l'adresse n'existe pas dans le système
     */
    public void updateMapping(String address, Firestation firestation) {
        if (!firestationRepository.existsByAddress(address)) {
            throw new IllegalArgumentException("Adresse non trouvée");
        }
        firestationRepository.updateFirestation(firestation);
    }

    /**
     * Supprime un ou plusieurs mappings caserne/adresse.
     *
     * <p>
     * Cette méthode offre deux modes de suppression :
     * <ul>
     * <li><strong>Par adresse :</strong> Supprime le mapping pour une adresse
     * spécifique</li>
     * <li><strong>Par numéro de caserne :</strong> Supprime tous les mappings
     * associés à un numéro de caserne</li>
     * </ul>
     *
     * <p>
     * <strong>Règles de validation :</strong>
     * <ul>
     * <li>Un seul paramètre doit être fourni (soit address, soit station)</li>
     * <li>Si les deux paramètres sont fournis, une exception est levée</li>
     * <li>Si aucun paramètre n'est fourni, une exception est levée</li>
     * </ul>
     *
     * @param address l'adresse dont on veut supprimer le mapping (peut être null si
     *                station est fourni)
     * @param station le numéro de caserne dont on veut supprimer tous les mappings
     *                (peut être null si address est fourni)
     * @throws IllegalArgumentException si les deux paramètres sont fournis ou si
     *                                  aucun paramètre n'est fourni
     */
    public void deleteMapping(String address, Integer station) {

        // Cas 1 : Aucun paramètre fourni
        if ((address == null || address.isBlank()) && (station == null)) {
            throw new IllegalArgumentException("Vous devez spécifier soit l'adresse soit le numéro de station");
        }

        // Cas 2 : Les deux paramètres fournis
        if ((address != null && !address.isBlank()) && station != null) {
            throw new IllegalArgumentException("Spécifiez soit l'adresse soit le numéro, pas les deux");
        }

        // Cas 3 : Suppression par adresse
        if (address != null && !address.isBlank()) {
            firestationRepository.deleteFirestationByAddress(address);
            return;
        }

        // Cas 4 : Suppression par station
        if (station != null) {
            firestationRepository.deleteFirestationByStation(station);
            return;
        }
    }
}