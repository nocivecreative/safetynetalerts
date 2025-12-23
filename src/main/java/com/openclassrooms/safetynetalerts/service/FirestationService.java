package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;

@Service
public class FirestationService {
    private final Logger logger = LoggerFactory.getLogger(FirestationService.class);

    @Autowired
    private FirestationRepository firestationRepository;

    @Autowired
    private PersonRepository personRepository;

    /**
     * Récupère une liste de personnes couvertes par une caserne
     * 
     * @param stationNumber Numéro de la caserne
     * @return Une liste de peronnes
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
     * Récupère un set de numéros de téléphones des personnes couvertes par une
     * caserne
     * 
     * @param stationNumber Numéro de la caserne
     * @return Set de numéros de téléphone uniques
     */
    public Set<String> getPhonesByStation(int stationNumber) {
        logger.debug("[SERVICE] Recherche des téléphones pour la station={}", stationNumber);

        List<String> addresses = firestationRepository.findAddressesByStation(stationNumber);

        return addresses.stream()
                .flatMap(address -> personRepository.findByAddress(address).stream())
                .map(Person::getPhone)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Ajout d'un mapping numéro de caserne/adresse
     * 
     * @param firestation Caserne
     * @throws IllegalArgumentException Si l'adresse de la caserne ou le numéro
     *                                  existe déjà
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
     * Met à jour le mapping numéro de caserne/adresse
     * 
     * @param firestation Caserne
     * @throws IllegalArgumentException Si l'adresse n'est pas trouvée
     */
    public void updateMapping(Firestation firestation) {
        if (!firestationRepository.existsByAddress(firestation.getAddress())) {
            throw new IllegalArgumentException("Adresse non trouvée");
        }
        firestationRepository.updateFirestation(firestation);
    }

    /**
     * Supprime le mapping numéro de caserne/adresse
     * 
     * @param address Adresse de caserne
     * @param station Numéro de caserne
     * @throws IllegalArgumentException Si à la fois l'adresse et le numéro sont
     *                                  fournis
     *                                  OU Si ni l'adresse ni le numéro sont fournis
     */
    public void deleteMapping(String address, Integer station) {

        if (address != null && station != null) {
            throw new IllegalArgumentException("Spécifiez soit l'adresse soit le numéro, pas les deux");
        }

        if (address != null) {
            firestationRepository.deleteFirestationByAddress(address);
            return;
        }

        if (station != null) {
            firestationRepository.deleteFirestationByStation(station);
            return;
        }

        throw new IllegalArgumentException("L'adresse ou le numéro doivent être fournis");
    }
}