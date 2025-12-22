package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationResidentDTO;
import com.openclassrooms.safetynetalerts.dto.phonealert.PhoneAlertResponseDTO;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class FirestationService {
    private final Logger logger = LoggerFactory.getLogger(FirestationService.class);

    // @Autowired
    // private JsonDataRepo dataRepo;

    @Autowired
    private FirestationRepository firestationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    /**
     * Récupère une liste de personnes couvertes par une caserne
     * 
     * @param stationNumber Numéro de la caserne
     * @return Une liste de peronnes
     */
    public FirestationCoverageResponseDTO getPersonsCoveredByStation(int stationNumber) {
        logger.debug("[SERVICE] Recherche des peronnes couvertes par la station={}", stationNumber);

        // Récupérer les adresses couvertes par la caserne
        List<String> coveredAddresses = firestationRepository.findAddressesByStation(stationNumber);

        // Récupére les personnes à ces adresses
        List<Person> coveredPersons = coveredAddresses.stream()
                .flatMap(address -> personRepository.findByAddress(address).stream())
                .toList();

        // Mapper vers DTO
        List<FirestationResidentDTO> personInfos = coveredPersons.stream()
                .map(person -> new FirestationResidentDTO(
                        person.getFirstName(),
                        person.getLastName(),
                        person.getAddress(),
                        person.getPhone()))
                .toList();

        // Calculer adultes et enfants
        int adultCount = 0;
        int childCount = 0;

        logger.debug("[SERVICE] Début du calcul de l'age des personnes couvertes par la station={}", stationNumber);
        for (Person person : coveredPersons) {
            int age = Utils.calculateAge(person, medicalRecordRepository);
            if (age <= 18) {
                childCount++;
            } else {
                adultCount++;
            }
        }
        return new FirestationCoverageResponseDTO(personInfos, adultCount, childCount);
    }

    /**
     * Récupère un set de numéros de téléphones des personnes couvertes par une
     * caserne
     * 
     * @param stationNumber Numéro de la caserne
     * @return Set de numéros de téléphone uniques
     */
    public PhoneAlertResponseDTO getPhoneOfPersonsCoveredByStation(int stationNumber) {
        logger.debug("[SERVICE] looking for phons of persons covered by stationNumber={}", stationNumber);

        // Récupérer les adresses couvertes par la caserne
        List<String> coveredAddresses = firestationRepository.findAddressesByStation(stationNumber);

        // Récupére les personnes à ces adresses
        List<Person> coveredPersons = coveredAddresses.stream()
                .flatMap(address -> personRepository.findByAddress(address).stream())
                .toList();

        // Mapper vers DTO
        Set<String> phoneList = new TreeSet<>();
        for (Person person : coveredPersons) {
            String phoneNumber = person.getPhone();
            phoneList.add(phoneNumber);
        }

        return new PhoneAlertResponseDTO(phoneList);
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