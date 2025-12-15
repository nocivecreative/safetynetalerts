package com.openclassrooms.safetynetalerts.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.dto.FirestationCoverageDTO;
import com.openclassrooms.safetynetalerts.dto.PersonInfoDTO;
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

        @Autowired
        private Utils utils;

        public FirestationCoverageDTO getPersonsCoveredByStation(String stationNumber) {
                DataFile data = dataRepo.loadData();

                // Récupérer les adresses couvertes par la caserne
                List<String> coveredAddresses = data.getFirestations().stream()
                                .filter(fs -> fs.getStation().equals(stationNumber))
                                .map(Firestation::getAddress)
                                .collect(Collectors.toList());

                // Récupérer les personnes à ces adresses
                List<Person> coveredPersons = data.getPersons().stream()
                                .filter(person -> coveredAddresses.contains(person.getAddress()))
                                .collect(Collectors.toList());

                // Mapper vers DTO
                List<PersonInfoDTO> personInfos = coveredPersons.stream()
                                .map(person -> new PersonInfoDTO(
                                                person.getFirstName(),
                                                person.getLastName(),
                                                person.getAddress(),
                                                person.getPhone()))
                                .collect(Collectors.toList());

                // Calculer adultes et enfants
                int adultCount = 0;
                int childCount = 0;

                for (Person person : coveredPersons) {
                        int age = utils.calculateAge(person, data.getMedicalrecords());
                        if (age <= 18) {
                                childCount++;
                        } else {
                                adultCount++;
                        }
                }
                logger.info("[SUCCESS] getPersonsCoveredByStation");
                return new FirestationCoverageDTO(personInfos, adultCount, childCount);
        }

}