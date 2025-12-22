package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.PersonProfile;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class PersonProfileService {

    @Autowired
    private PersonService personService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private Utils utils;

    public List<PersonProfile> getProfilesByLastName(String lastName) {

        List<Person> persons = personService.getPersonsByLastName(lastName);
        List<PersonProfile> profiles = new ArrayList<>();

        for (Person person : persons) {
            Optional<MedicalRecord> record = medicalRecordService.getMedicalRecord(
                    person.getFirstName(), person.getLastName());

            int age = record.isPresent()
                    ? utils.calculateAge(person)
                    : -1;

            profiles.add(new PersonProfile(
                    person,
                    record.orElse(null),
                    age));
        }

        return profiles;
    }

    public List<PersonProfile> getProfilesByAddress(String address) {

        List<Person> persons = personService.getPersonsByAddress(address);
        List<PersonProfile> profiles = new ArrayList<>();

        for (Person person : persons) {
            Optional<MedicalRecord> record = medicalRecordService.getMedicalRecord(
                    person.getFirstName(), person.getLastName());

            int age = utils.calculateAge(person);

            profiles.add(new PersonProfile(
                    person,
                    record.orElse(null),
                    age));
        }

        return profiles;
    }
}