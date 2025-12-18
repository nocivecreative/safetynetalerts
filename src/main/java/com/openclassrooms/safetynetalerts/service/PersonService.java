package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.DataRepo;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class PersonService {
        private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

        @Autowired
        private DataRepo dataRepo;

        public ChildAlertResponseDTO getChildrenLivingAtAdress(String address) {
                DataFile data = dataRepo.loadData();

                List<ChildInfoDTO> children = new ArrayList<>();
                List<HouseholdMemberDTO> householdMembers = new ArrayList<>();

                // Récupérer la liste des enfants vivant à cette adresse
                List<Person> personLivingIn = data.getPersons().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .collect(Collectors.toList());

                // Pour chaque personne, récupération du dossier medical en O(N+M)
                for (Person person : personLivingIn) {
                        MedicalRecord medicalRecord = data.getMedicalrecords().stream()
                                        .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                                                        && mr.getLastName().equals(person.getLastName()))
                                        .findFirst()
                                        .orElse(null);

                        System.out.println(medicalRecord);

                        int age = Utils.calculateAge(person, data.getMedicalrecords());
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

                // Récupère les dossiers médicaux des personnes vivant à l'adresse donnée (NON
                // car complexité O(N*M))
                /*
                 * List<MedicalRecord> personMedicalRecords = data.getMedicalrecords().stream()
                 * .filter(mr -> mr.getFirstName().equals("John"))
                 * .collect(Collectors.toList());
                 */
                System.out.println(householdMembers);
                System.out.println(children);

                logger.info("[SUCCESS] getChildrenLivingAtAdress");
                return new ChildAlertResponseDTO(children);
        }

        public FireAddressResponseDTO getPersonAndMedicalHistoryLivingAtAdress(String address) {
                DataFile data = dataRepo.loadData();

                // Récupérer la liste des enfants vivant à cette adresse
                List<Person> personLivingIn = data.getPersons().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .collect(Collectors.toList());

                int firestationNumber = data.getFirestations().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .map(Firestation::getStation) // ou fs -> fs.getStation()
                                .findFirst()
                                .orElse(null);

                List<FireAddressResidentDTO> personFireList = new ArrayList<>();
                for (Person person : personLivingIn) {
                        int age = Utils.calculateAge(person, data.getMedicalrecords());

                        List<String> medicationList = data.getMedicalrecords().stream()
                                        .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                                                        && mr.getLastName().equals(person.getLastName()))
                                        .map(MedicalRecord::getMedications)
                                        .findFirst()
                                        .orElse(null); // ou .orElse(Collections.emptyList());
                        List<String> allergiesList = data.getMedicalrecords().stream()
                                        .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                                                        && mr.getLastName().equals(person.getLastName()))
                                        .map(MedicalRecord::getAllergies)
                                        .findFirst()
                                        .orElse(null); // ou .orElse(Collections.emptyList());

                        MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(medicationList, allergiesList);

                        FireAddressResidentDTO personFire = new FireAddressResidentDTO(
                                        person.getLastName(),
                                        person.getPhone(),
                                        medicalHistory,
                                        age);

                        personFireList.add(personFire);
                }
                logger.info("[SUCCESS] getPersonAndMedicalHistoryLivingAtAdress");
                return new FireAddressResponseDTO(personFireList, firestationNumber);
        }

        public FloodStationsResponseDTO getPersonAndMedicalHistoryCoveredByStations(List<Integer> stations) {
                DataFile data = dataRepo.loadData();
                List<FloodHouseholdDTO> households = new ArrayList<>();

                // Récupére toutes les adresses couvertes par les stations
                Set<String> firestationAddresses = new HashSet<>();
                for (int station : stations) {
                        firestationAddresses.addAll(
                                        data.getFirestations().stream()
                                                        .filter(fs -> fs.getStation() == station)
                                                        .map(Firestation::getAddress)
                                                        .collect(Collectors.toList()));
                }

                // Pour chaque adresse, créer un floodHouseDTO
                for (String address : firestationAddresses) {

                        List<FloodResidentDTO> floodPersonInfoList = new ArrayList<>();

                        // Récupérer toutes les personnes à cette adresse
                        List<Person> personsAtAddress = data.getPersons().stream()
                                        .filter(p -> p.getAddress().equals(address))
                                        .collect(Collectors.toList());

                        // Pour chaque personne, créer un FloodPersonInfoDTO
                        for (Person person : personsAtAddress) {

                                // Récupérer le MedicalRecord UNE SEULE FOIS
                                MedicalRecord medicalRecord = data.getMedicalrecords().stream()
                                                .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                                                                && mr.getLastName().equals(person.getLastName()))
                                                .findFirst()
                                                .orElse(null);

                                // Extraire les informations du medical record
                                List<String> medications = medicalRecord != null
                                                ? medicalRecord.getMedications()
                                                : Collections.emptyList();

                                List<String> allergies = medicalRecord != null
                                                ? medicalRecord.getAllergies()
                                                : Collections.emptyList();

                                int age = medicalRecord != null
                                                ? Utils.calculateAge(person, data.getMedicalrecords())
                                                : 0;

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
                DataFile data = dataRepo.loadData();
                List<PersonMedicalProfileDTO> personInfolastNameDTOs = new ArrayList<>();

                // Récupérer toutes les personnes à cette adresse
                List<Person> personsAtAddress = data.getPersons().stream()
                                .filter(p -> p.getLastName().equals(lastName))
                                .collect(Collectors.toList());

                for (Person person : personsAtAddress) {

                        // Récupérer le MedicalRecord UNE SEULE FOIS
                        MedicalRecord medicalRecord = data.getMedicalrecords().stream()
                                        .filter(mr -> mr.getFirstName().equals(person.getFirstName())
                                                        && mr.getLastName().equals(person.getLastName()))
                                        .findFirst()
                                        .orElse(null);
                        // Extraire les informations du medical record
                        List<String> medications = medicalRecord != null
                                        ? medicalRecord.getMedications()
                                        : Collections.emptyList();

                        List<String> allergies = medicalRecord != null
                                        ? medicalRecord.getAllergies()
                                        : Collections.emptyList();

                        int age = medicalRecord != null
                                        ? Utils.calculateAge(person, data.getMedicalrecords())
                                        : 0;

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
                DataFile data = dataRepo.loadData();

                // Récupérer toutes les personnes à cette adresse
                Set<String> emailAddresses = new TreeSet<>();
                List<Person> personsInCity = data.getPersons().stream()
                                .filter(p -> p.getCity().equals(city))
                                .collect(Collectors.toList());

                for (Person person : personsInCity) {

                        emailAddresses.add(person.getEmail());

                }

                return new CommunityEmailResponseDTO(emailAddresses);
        }
}