package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResponseDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.childalert.HouseholdMemberDTO;
import com.openclassrooms.safetynetalerts.dto.commons.MedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.communityemail.CommunityEmailResponseDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResidentDTO;
import com.openclassrooms.safetynetalerts.dto.fireaddress.FireAddressResponseDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodHouseholdDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodResidentDTO;
import com.openclassrooms.safetynetalerts.dto.floodstations.FloodStationsResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonInfoResponseDTO;
import com.openclassrooms.safetynetalerts.dto.personinfo.PersonMedicalProfileDTO;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;
import com.openclassrooms.safetynetalerts.repository.PersonRepository;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class PersonService {
    private final Logger logger = LoggerFactory.getLogger(PersonService.class);

    // @Autowired
    // private DataRepo dataRepo;

    @Autowired
    private FirestationRepository firestationRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    public ChildAlertResponseDTO getChildrenLivingAtAdress(String address) {
        logger.debug("[SERVICE] looking for perons living at address={}", address);

        // ataFile data = dataRepo.loadData();

        List<ChildInfoDTO> children = new ArrayList<>();
        List<HouseholdMemberDTO> householdMembers = new ArrayList<>();

        // Récupérer la liste des personnes vivant à cette adresse
        List<Person> personLivingIn = personRepository.findByAddress(address);

        // Pour chaque personne, récupération du dossier medical (en O(N+M))
        logger.debug("[SERVICE] Starting age computing for sorting children/adults living at address={}",
                address);
        for (Person person : personLivingIn) {

            int age = Utils.calculateAge(person, medicalRecordRepository);
            if (age <= 18) {
                // Créer et ajouter un ChildInfoDTO
                children.add(new ChildInfoDTO(
                        person.getFirstName(),
                        person.getLastName(),
                        age,
                        householdMembers));
            } else {
                // Créer et ajouter un HouseholdMemberDTO
                householdMembers.add(new HouseholdMemberDTO(
                        person.getFirstName(),
                        person.getLastName()));
            }

        }

        // TODO : inclure dans prez
        // Récupère les dossiers médicaux des personnes vivant à l'adresse donnée (NON
        // car complexité O(N*M))
        /*
         * List<MedicalRecord> personMedicalRecords = data.getMedicalrecords().stream()
         * .filter(mr -> mr.getFirstName().equals("John"))
         * .toList();
         */

        return new ChildAlertResponseDTO(children);
    }

    public FireAddressResponseDTO getPersonAndMedicalHistoryLivingAtAdress(String address) {

        logger.debug("[SERVICE] looking for perons living at address={}", address);

        // Récupérer la liste des enfants vivant à cette adresse
        List<Person> personLivingIn = personRepository.findByAddress(address);

        // Récupère le numéro de la station qui couvre cette adresse
        Integer firestationNumber = firestationRepository
                .findStationByAddress(address)
                .orElse(-1); // TODO : Voir si throw error à la place de -1

        List<FireAddressResidentDTO> personFireList = new ArrayList<>();
        for (Person person : personLivingIn) {
            int age = Utils.calculateAge(person, medicalRecordRepository);

            Optional<MedicalRecord> medicalRecordOpt = medicalRecordRepository.findByFirstNameAndLastName(
                    person.getFirstName(),
                    person.getLastName());

            List<String> medications = medicalRecordOpt
                    .map(MedicalRecord::getMedications)
                    .orElse(Collections.emptyList());

            List<String> allergies = medicalRecordOpt
                    .map(MedicalRecord::getAllergies)
                    .orElse(Collections.emptyList());

            MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(medications, allergies);

            FireAddressResidentDTO personFire = new FireAddressResidentDTO(
                    person.getLastName(),
                    person.getPhone(),
                    medicalHistory,
                    age);

            personFireList.add(personFire);
        }
        return new FireAddressResponseDTO(personFireList, firestationNumber);
    }

    public FloodStationsResponseDTO getPersonAndMedicalHistoryCoveredByStations(List<Integer> stations) {

        logger.debug("[SERVICE] looking for perons covered by stations={}", stations);

        List<FloodHouseholdDTO> households = new ArrayList<>();

        // Récupére toutes les adresses couvertes par les stations
        Set<String> addresses = firestationRepository.findAddressesByStations(stations);

        // Pour chaque adresse, créer une liste des personnes
        for (String address : addresses) {

            List<FloodResidentDTO> floodPersonInfoList = new ArrayList<>();

            // Récupérer toutes les personnes à cette adresse
            List<Person> personsAtAddress = personRepository.findAll().stream()
                    .filter(p -> p.getAddress().equals(address))
                    .toList();

            // Pour chaque personne, créer un FloodPersonInfoDTO
            for (Person person : personsAtAddress) {

                // Récupérer le MedicalRecord UNE SEULE FOIS
                Optional<MedicalRecord> medicalRecordOpt = medicalRecordRepository
                        .findByFirstNameAndLastName(
                                person.getFirstName(),
                                person.getLastName());

                // Extraire les informations du medical record
                List<String> medications = medicalRecordOpt
                        .map(MedicalRecord::getMedications)
                        .orElse(Collections.emptyList());

                List<String> allergies = medicalRecordOpt
                        .map(MedicalRecord::getAllergies)
                        .orElse(Collections.emptyList());

                int age = Utils.calculateAge(person, medicalRecordRepository);

                // Créer le DTO de l'historique médical
                MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(
                        medications,
                        allergies);

                // Créer le DTO de la personne avec toutes les infos
                FloodResidentDTO personInfo = new FloodResidentDTO(
                        person.getLastName(),
                        person.getPhone(),
                        age,
                        medicalHistory);

                floodPersonInfoList.add(personInfo);
            }

            // Créer un floodHouseDTO pour cette adresse
            FloodHouseholdDTO floodHouseDTO = new FloodHouseholdDTO(address, floodPersonInfoList);
            households.add(floodHouseDTO);
        }

        return new FloodStationsResponseDTO(households);
    }

    public PersonInfoResponseDTO getPersonInfosAndMedicalHistoryByLastName(String lastName) {
        logger.debug("[SERVICE] looking for perons infos and medical history for lastname={}", lastName);

        List<PersonMedicalProfileDTO> personInfolastNameDTOs = new ArrayList<>();

        // Récupérer toutes les personnes avec ce nom de famille
        List<Person> personsAtAddress = personRepository.findByLastName(lastName);

        for (Person person : personsAtAddress) {

            // Récupérer le MedicalRecord
            Optional<MedicalRecord> medicalRecordOpt = medicalRecordRepository
                    .findByFirstNameAndLastName(person.getFirstName(), person.getLastName());
            // Extraire les informations du medical record
            List<String> medications = medicalRecordOpt
                    .map(MedicalRecord::getMedications)
                    .orElse(Collections.emptyList());

            List<String> allergies = medicalRecordOpt
                    .map(MedicalRecord::getAllergies)
                    .orElse(Collections.emptyList());

            int age = medicalRecordOpt.isPresent()
                    ? Utils.calculateAge(person, medicalRecordRepository)
                    : -1;

            // Créer le DTO de l'historique médical
            MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(
                    medications,
                    allergies);

            PersonMedicalProfileDTO personInfolastNameDTO = new PersonMedicalProfileDTO(
                    person.getLastName(),
                    person.getAddress(),
                    age, person.getEmail(),
                    medicalHistory);

            personInfolastNameDTOs.add(personInfolastNameDTO);
        }

        return new PersonInfoResponseDTO(personInfolastNameDTOs);
    }

    public CommunityEmailResponseDTO getEmailsaddressesForCityResidents(String city) {

        logger.debug("[SERVICE] looking for emails of resident in city={}", city);
        // Récupérer toutes les personnes à cette adresse
        return new CommunityEmailResponseDTO(personRepository.findEmailsByCity(city));
    }

    public void addPerson(Person person) {

        if (personRepository.personExists(person.getFirstName(), person.getLastName())) {
            logger.error("[SERVICE] Person already exist: {} {}",
                    person.getFirstName(), person.getLastName());
            throw new IllegalArgumentException("Person already exist");
        }

        personRepository.addPerson(person);
    }

    public void updatePerson(Person person) {
        if (!personRepository.personExists(person.getFirstName(), person.getLastName())) {
            logger.error("[SERVICE] Person not found: {} {}",
                    person.getFirstName(), person.getLastName());
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.updatePerson(person);
    }

    public void deletePerson(String firstName, String lastName) {
        if (!personRepository.personExists(firstName, lastName)) {
            logger.error("[SERVICE] Person not found: {} {}",
                    firstName, lastName);
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.deletePerson(firstName, lastName);
    }

}