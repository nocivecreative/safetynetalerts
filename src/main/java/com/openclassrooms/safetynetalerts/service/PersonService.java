package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.dto.ChildAlertDTO;
import com.openclassrooms.safetynetalerts.dto.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.FireDTO;
import com.openclassrooms.safetynetalerts.dto.FloodDTO;
import com.openclassrooms.safetynetalerts.dto.FloodMedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.FloodPersonInfoDTO;
import com.openclassrooms.safetynetalerts.dto.HouseholdMemberDTO;
import com.openclassrooms.safetynetalerts.dto.MedicalHistoryDTO;
import com.openclassrooms.safetynetalerts.dto.PersonFireDTO;
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

        public ChildAlertDTO getChildrenLivingAtAdress(String address) {
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

                        int age = Utils.calculateAge(person, medicalRecord);
                        if (age <= 18) {
                                // Créer et ajouter un ChildInfoDTO
                                children.add(new ChildInfoDTO(
                                                person.getFirstName(),
                                                person.getLastName(),
                                                age // ← L'âge est capturé ici !
                                ));
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
                return new ChildAlertDTO(children, householdMembers);
        }

        public FireDTO getPersonAndMedicalHistoryLivingAtAdress(String address) {
                DataFile data = dataRepo.loadData();

                // Récupérer la liste des enfants vivant à cette adresse
                List<Person> personLivingIn = data.getPersons().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .collect(Collectors.toList());

                String firestationNumber = data.getFirestations().stream()
                                .filter(p -> p.getAddress().equals(address))
                                .map(Firestation::getStation) // ou fs -> fs.getStation()
                                .findFirst()
                                .orElse(null);

                List<PersonFireDTO> personFireList = new ArrayList<>();
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

                        PersonFireDTO personFire = new PersonFireDTO(
                                        person.getLastName(),
                                        person.getPhone(),
                                        medicalHistory,
                                        age);

                        personFireList.add(personFire);
                }
                logger.info("[SUCCESS] getPersonAndMedicalHistoryLivingAtAdress");
                return new FireDTO(personFireList, firestationNumber);
        }

        public List<FloodDTO> getPersonAndMedicalHistoryCoveredByStations(List<String> stations) {
                DataFile data = dataRepo.loadData();
                List<FloodDTO> floodDTOs = new ArrayList<>();

                // Récupére toutes les adresses couvertes par les stations
                Set<String> firestationAddresses = new HashSet<>();
                for (String station : stations) {
                        firestationAddresses.addAll(
                                        data.getFirestations().stream()
                                                        .filter(fs -> fs.getStation().equals(station))
                                                        .map(Firestation::getAddress)
                                                        .collect(Collectors.toList()));
                }

                // Pour chaque adresse, créer un FloodDTO
                for (String address : firestationAddresses) {

                        List<FloodPersonInfoDTO> floodPersonInfoList = new ArrayList<>();

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
                                FloodMedicalHistoryDTO medicalHistory = new FloodMedicalHistoryDTO(
                                                medications,
                                                allergies);

                                // Créer le DTO de la personne avec toutes les infos
                                FloodPersonInfoDTO personInfo = new FloodPersonInfoDTO(
                                                person.getLastName(),
                                                person.getPhone(),
                                                age,
                                                medicalHistory);

                                floodPersonInfoList.add(personInfo);
                        }

                        // Créer un FloodDTO pour cette adresse
                        FloodDTO floodDTO = new FloodDTO(address, floodPersonInfoList);
                        floodDTOs.add(floodDTO);
                }

                return floodDTOs;
        }
}