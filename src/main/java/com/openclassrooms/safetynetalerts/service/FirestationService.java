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
import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.DataRepo;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class FirestationService {
    private final Logger logger = LoggerFactory.getLogger(FirestationService.class);

    @Autowired
    private DataRepo dataRepo;

    public FirestationCoverageResponseDTO getPersonsCoveredByStation(int stationNumber) {
        DataFile data = dataRepo.loadData();

        logger.debug("[SERVICE] looking for perons covered by stationNumber={}", stationNumber);

        // Récupérer les adresses couvertes par la caserne
        List<String> coveredAddresses = data.getFirestations().stream()
                .filter(fs -> fs.getStation() == stationNumber)
                .map(Firestation::getAddress)
                .toList();

        // Récupérer les personnes à ces adresses
        List<Person> coveredPersons = data.getPersons().stream()
                .filter(person -> coveredAddresses.contains(person.getAddress()))
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

        logger.debug("[SERVICE] Starting age computing for perons covered by stationNumber={}", stationNumber);
        for (Person person : coveredPersons) {
            int age = Utils.calculateAge(person, data.getMedicalrecords());
            if (age <= 18) {
                childCount++;
            } else {
                adultCount++;
            }
        }
        return new FirestationCoverageResponseDTO(personInfos, adultCount, childCount);
    }

    public PhoneAlertResponseDTO getPhoneOfPersonsCoveredByStation(int stationNumber) {
        DataFile data = dataRepo.loadData();

        logger.debug("[SERVICE] looking for phons of persons covered by stationNumber={}", stationNumber);
        // Récupérer les adresses couvertes par la caserne
        List<String> coveredAddresses = data.getFirestations().stream()
                .filter(fs -> fs.getStation() == stationNumber)
                .map(Firestation::getAddress)
                .toList();

        // Récupérer les personnes à ces adresses
        List<Person> coveredPersons = data.getPersons().stream()
                .filter(person -> coveredAddresses.contains(person.getAddress()))
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
        if (dataRepo.firestationAddressExists(firestation.getAddress())) {
            throw new IllegalArgumentException("Address already mapped");
        }
        dataRepo.addFirestation(firestation);
    }

    public void updateMapping(Firestation firestation) {
        if (!dataRepo.firestationAddressExists(firestation.getAddress())) {
            throw new IllegalArgumentException("Address not found");
        }
        dataRepo.updateFirestation(firestation);
    }

    public void deleteMapping(String address, Integer station) {

        if (address != null && station != null) {
            throw new IllegalArgumentException("Specify either address or station, not both");
        }

        if (address != null) {
            dataRepo.deleteFirestationByAddress(address);
            return;
        }

        if (station != null) {
            dataRepo.deleteFirestationByStation(station);
            return;
        }

        throw new IllegalArgumentException("Address or station must be provided");
    }
}