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

    public PhoneAlertResponseDTO getPhoneOfPersonsCoveredByStation(int stationNumber) {
        logger.debug("[SERVICE] looking for phons of persons covered by stationNumber={}", stationNumber);

        // DataFile data = dataRepo.loadData();

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

    public void addMapping(Firestation firestation) {
        if (firestationRepository.existsByAddress(firestation.getAddress())) {
            throw new IllegalArgumentException("L'adresse existe déjà");
        }
        firestationRepository.addFirestation(firestation);
    }

    public void updateMapping(Firestation firestation) {
        if (!firestationRepository.existsByAddress(firestation.getAddress())) {
            throw new IllegalArgumentException("Adresse non trouvée");
        }
        firestationRepository.updateFirestation(firestation);
    }

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