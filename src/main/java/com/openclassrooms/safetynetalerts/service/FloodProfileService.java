package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.HouseholdProfile;
import com.openclassrooms.safetynetalerts.PersonProfile;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class FloodProfileService {

    @Autowired
    private FirestationRepository firestationRepository;

    @Autowired
    private PersonService personService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private Utils utils;

    public List<HouseholdProfile> getHouseholdsByStations(List<Integer> stationNumbers) {
        Map<String, List<PersonProfile>> householdsMap = new LinkedHashMap<>();

        for (Integer stationNumber : stationNumbers) {
            List<String> addresses = firestationRepository.findAddressesByStation(stationNumber);

            for (String address : addresses) {
                List<Person> persons = personService.getPersonsByAddress(address);

                List<PersonProfile> personProfiles = persons.stream()
                        .map(person -> {
                            Optional<MedicalRecord> record = medicalRecordService
                                    .getMedicalRecord(person.getFirstName(), person.getLastName());
                            int age = utils.calculateAge(person);
                            return new PersonProfile(person, record.orElse(null), age);
                        })
                        .toList();

                householdsMap.merge(address, personProfiles,
                        (existing, newList) -> {
                            List<PersonProfile> combined = new ArrayList<>(existing);
                            combined.addAll(newList);
                            return combined;
                        });
            }
        }

        // Transformer map en liste de foyers
        return householdsMap.entrySet().stream()
                .map(e -> new HouseholdProfile(e.getKey(), e.getValue()))
                .toList();
    }
}
