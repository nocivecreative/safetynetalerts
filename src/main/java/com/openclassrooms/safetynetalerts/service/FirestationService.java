package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class FirestationService {
        private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

        @Autowired
        private DataRepo dataRepo;

        public FirestationCoverageResponseDTO getPersonsCoveredByStation(int stationNumber) {
                DataFile data = dataRepo.loadData();

                // Récupérer les adresses couvertes par la caserne
                List<String> coveredAddresses = data.getFirestations().stream()
                                .filter(fs -> fs.getStation() == stationNumber)
                                .map(Firestation::getAddress)
                                .collect(Collectors.toList());

                // Récupérer les personnes à ces adresses
                List<Person> coveredPersons = data.getPersons().stream()
                                .filter(person -> coveredAddresses.contains(person.getAddress()))
                                .collect(Collectors.toList());

                // Mapper vers DTO
                List<FirestationResidentDTO> personInfos = coveredPersons.stream()
                                .map(person -> new FirestationResidentDTO(
                                                person.getFirstName(),
                                                person.getLastName(),
                                                person.getAddress(),
                                                person.getPhone()))
                                .collect(Collectors.toList());

                // Calculer adultes et enfants
                int adultCount = 0;
                int childCount = 0;

                for (Person person : coveredPersons) {
                        int age = Utils.calculateAge(person, data.getMedicalrecords());
                        if (age <= 18) {
                                childCount++;
                        } else {
                                adultCount++;
                        }
                }
                logger.info("[SUCCESS] getPersonsCoveredByStation");
                return new FirestationCoverageResponseDTO(personInfos, adultCount, childCount);
        }

        public PhoneAlertResponseDTO getPhoneOfPersonsCoveredByStation(int stationNumber) {
                DataFile data = dataRepo.loadData();

                // Récupérer les adresses couvertes par la caserne
                List<String> coveredAddresses = data.getFirestations().stream()
                                .filter(fs -> fs.getStation() == stationNumber)
                                .map(Firestation::getAddress)
                                .collect(Collectors.toList());

                // Récupérer les personnes à ces adresses
                List<Person> coveredPersons = data.getPersons().stream()
                                .filter(person -> coveredAddresses.contains(person.getAddress()))
                                .collect(Collectors.toList());

                // Mapper vers DTO
                Set<String> phoneList = new TreeSet<>();
                for (Person person : coveredPersons) {
                        String phoneNumber = person.getPhone();
                        phoneList.add(phoneNumber);
                }

                return new PhoneAlertResponseDTO(phoneList);
        }
}