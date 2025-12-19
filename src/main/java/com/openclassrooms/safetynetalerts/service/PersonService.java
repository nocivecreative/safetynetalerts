package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import com.openclassrooms.safetynetalerts.utils.Utils;

@Service
public class PersonService {
        private final Logger logger = LoggerFactory.getLogger(PersonService.class);

        @Autowired
        private DataRepo dataRepo;

        public ChildAlertResponseDTO getChildrenLivingAtAdress(String address) {
                DataFile data = dataRepo.loadData();

                logger.debug("[SERVICE] looking for perons living at address={}", address);

                List<ChildInfoDTO> children = new ArrayList<>();
                List<HouseholdMemberDTO> householdMembers = new ArrayList<>();

                // Récupérer la liste des enfants vivant à cette adresse
                List<Person> personLivingIn = data.getPersons().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .toList();

                // Pour chaque personne, récupération du dossier medical en O(N+M)
                logger.debug("[SERVICE] Starting age computing for sorting children/adults living at address={}",
                                address);
                for (Person person : personLivingIn) {

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
                DataFile data = dataRepo.loadData();

                logger.debug("[SERVICE] looking for perons living at address={}", address);
                // Récupérer la liste des enfants vivant à cette adresse
                List<Person> personLivingIn = data.getPersons().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .toList();

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
                return new FireAddressResponseDTO(personFireList, firestationNumber);
        }

        public FloodStationsResponseDTO getPersonAndMedicalHistoryCoveredByStations(List<Integer> stations) {
                DataFile data = dataRepo.loadData();
                List<FloodHouseholdDTO> households = new ArrayList<>();

                logger.debug("[SERVICE] looking for perons covered by stations={}", stations);
                // Récupére toutes les adresses couvertes par les stations
                Set<String> firestationAddresses = new HashSet<>();
                for (int station : stations) {
                        firestationAddresses.addAll(
                                        data.getFirestations().stream()
                                                        .filter(fs -> fs.getStation() == station)
                                                        .map(Firestation::getAddress)
                                                        .toList());
                }

                // Pour chaque adresse, créer une liste des personnes
                for (String address : firestationAddresses) {

                        List<FloodResidentDTO> floodPersonInfoList = new ArrayList<>();

                        // Récupérer toutes les personnes à cette adresse
                        List<Person> personsAtAddress = data.getPersons().stream()
                                        .filter(p -> p.getAddress().equals(address))
                                        .toList();

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

                logger.debug("[SERVICE] looking for perons infos and medical history for lastname={}", lastName);
                // Récupérer toutes les personnes à cette adresse
                List<Person> personsAtAddress = data.getPersons().stream()
                                .filter(p -> p.getLastName().equals(lastName))
                                .toList();

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

                logger.debug("[SERVICE] looking for emails of resident in city={}", city);
                // Récupérer toutes les personnes à cette adresse
                Set<String> emailAddresses = new TreeSet<>();
                List<Person> personsInCity = data.getPersons().stream()
                                .filter(p -> p.getCity().equals(city))
                                .toList();

                for (Person person : personsInCity) {

                        emailAddresses.add(person.getEmail());

                }

                return new CommunityEmailResponseDTO(emailAddresses);
        }

        public void addPerson(Person person) {

                logger.debug("[SERVICE] Ajout de la personne {} {}",
                                person.getFirstName(), person.getLastName());

                if (dataRepo.personExists(person.getFirstName(), person.getLastName())) {
                        logger.error("[SERVICE] La personne existe déjà: {} {}",
                                        person.getFirstName(), person.getLastName());
                        throw new IllegalArgumentException("La personne existe déjà");
                }

                dataRepo.addPerson(person);

                logger.debug("[SERVICE] Personnee ajoutéee correctement: {} {}",
                                person.getFirstName(), person.getLastName());
        }

        public void updatePerson(Person person) {
                logger.debug("[SERVICE] Mise à jour de la personne {} {}",
                                person.getFirstName(), person.getLastName());
                if (!dataRepo.personExists(person.getFirstName(), person.getLastName())) {
                        logger.error("[SERVICE] Personne non trouvée: {} {}",
                                        person.getFirstName(), person.getLastName());
                        throw new IllegalArgumentException("Personne non trouvée");
                }
                dataRepo.updatePerson(person);
        }

        public void deletePerson(String firstName, String lastName) {
                if (!dataRepo.personExists(firstName, lastName)) {
                        logger.error("[SERVICE] Personne non trouvée: {} {}",
                                        firstName, lastName);
                        throw new IllegalArgumentException("Personne non trouvée");
                }
                dataRepo.deletePerson(firstName, lastName);
        }

}