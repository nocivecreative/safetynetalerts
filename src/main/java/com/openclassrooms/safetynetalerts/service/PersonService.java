package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.dto.ChildAlertDTO;
import com.openclassrooms.safetynetalerts.dto.ChildInfoDTO;
import com.openclassrooms.safetynetalerts.dto.FireDTO;
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

        @Autowired
        private Utils utils;

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

                        int age = utils.calculateAge(person, medicalRecord);
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
                        int age = utils.calculateAge(person, data.getMedicalrecords());

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

}