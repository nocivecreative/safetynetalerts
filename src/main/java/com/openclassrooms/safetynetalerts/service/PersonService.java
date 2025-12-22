package com.openclassrooms.safetynetalerts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.dto.childalert.ChildAlertResult;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.repository.FirestationRepository;
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
    private Utils utils;

    /**
     * Récupère une liste des enfants habitants à une adresse donnée.
     * 
     * @param address Adresse
     * @return Liste d'enfants
     */
    public ChildAlertResult getPersonByAddress(String address) {
        logger.debug("[SERVICE] looking for perons living at address={}", address);

        // ataFile data = dataRepo.loadData();

        List<Person> children = new ArrayList<>();
        List<Person> householdMembers = new ArrayList<>();

        // Récupérer la liste des personnes vivant à cette adresse
        List<Person> personLivingIn = personRepository.findByAddress(address);

        // Pour chaque personne, récupération du dossier medical (en O(N+M))
        logger.debug("[SERVICE] Starting age computing for sorting children/adults living at address={}",
                address);
        for (Person person : personLivingIn) {

            int age = utils.calculateAge(person);
            if (age <= 18) {
                // Créer et ajouter un ChildInfoDTO
                children.add(person);
            } else {
                // Créer et ajouter un HouseholdMemberDTO
                householdMembers.add(person);
            }

        }

        return new ChildAlertResult(children, householdMembers);
    }

    /**
     * Récupère la liste de toutes les personnes habitant à une adresse, leur
     * historique médical et le numéro de la caserne qui les couvrent
     * 
     * @param address Adresse
     * @return Liste des personnes, leur historique médical et numéro de caserne
     */
    /*
     * public FireAddressResponseDTO getPersonAndMedicalHistoryLivingAtAdress(String
     * address) {
     * 
     * logger.debug("[SERVICE] looking for perons living at address={}", address);
     * 
     * // Récupérer la liste des enfants vivant à cette adresse
     * List<Person> personLivingIn = personRepository.findByAddress(address);
     * 
     * // Récupère le numéro de la station qui couvre cette adresse
     * Integer firestationNumber = firestationRepository
     * .findStationByAddress(address)
     * .orElse(-1); // TODO : Voir si throw error à la place de -1
     * 
     * List<FireAddressResidentDTO> personFireList = new ArrayList<>();
     * for (Person person : personLivingIn) {
     * int age = utils.calculateAge(person);
     * 
     * Optional<MedicalRecord> medicalRecordOpt =
     * medicalRecordRepository.findByFirstNameAndLastName(
     * person.getFirstName(),
     * person.getLastName());
     * 
     * List<String> medications = medicalRecordOpt
     * .map(MedicalRecord::getMedications)
     * .orElse(Collections.emptyList());
     * 
     * List<String> allergies = medicalRecordOpt
     * .map(MedicalRecord::getAllergies)
     * .orElse(Collections.emptyList());
     * 
     * MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(medications,
     * allergies);
     * 
     * FireAddressResidentDTO personFire = new FireAddressResidentDTO(
     * person.getLastName(),
     * person.getPhone(),
     * medicalHistory,
     * age);
     * 
     * personFireList.add(personFire);
     * }
     * return new FireAddressResponseDTO(personFireList, firestationNumber);
     * }
     */

    /**
     * Récupère la liste de toutes les personnes et leur historique médical qui sont
     * couvertes par une caserne
     * 
     * @param stations Liste de numéros de casernes
     * @return Liste des personnes couvertes et leur historique médical
     */
    /*
     * public FloodStationsResponseDTO
     * getPersonAndMedicalHistoryCoveredByStations(List<Integer> stations) {
     * 
     * logger.debug("[SERVICE] looking for perons covered by stations={}",
     * stations);
     * 
     * List<FloodHouseholdDTO> households = new ArrayList<>();
     * 
     * // Récupére toutes les adresses couvertes par les stations
     * Set<String> addresses =
     * firestationRepository.findAddressesByStations(stations);
     * 
     * // Pour chaque adresse, créer une liste des personnes
     * for (String address : addresses) {
     * 
     * List<FloodResidentDTO> floodPersonInfoList = new ArrayList<>();
     * 
     * // Récupérer toutes les personnes à cette adresse
     * List<Person> personsAtAddress = personRepository.findAll().stream()
     * .filter(p -> p.getAddress().equals(address))
     * .toList();
     * 
     * // Pour chaque personne, créer un FloodPersonInfoDTO
     * for (Person person : personsAtAddress) {
     * 
     * // Récupérer le MedicalRecord UNE SEULE FOIS
     * Optional<MedicalRecord> medicalRecordOpt = medicalRecordRepository
     * .findByFirstNameAndLastName(
     * person.getFirstName(),
     * person.getLastName());
     * 
     * // Extraire les informations du medical record
     * List<String> medications = medicalRecordOpt
     * .map(MedicalRecord::getMedications)
     * .orElse(Collections.emptyList());
     * 
     * List<String> allergies = medicalRecordOpt
     * .map(MedicalRecord::getAllergies)
     * .orElse(Collections.emptyList());
     * 
     * int age = utils.calculateAge(person);
     * 
     * // Créer le DTO de l'historique médical
     * MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(
     * medications,
     * allergies);
     * 
     * // Créer le DTO de la personne avec toutes les infos
     * FloodResidentDTO personInfo = new FloodResidentDTO(
     * person.getLastName(),
     * person.getPhone(),
     * age,
     * medicalHistory);
     * 
     * floodPersonInfoList.add(personInfo);
     * }
     * 
     * // Créer un floodHouseDTO pour cette adresse
     * FloodHouseholdDTO floodHouseDTO = new FloodHouseholdDTO(address,
     * floodPersonInfoList);
     * households.add(floodHouseDTO);
     * }
     * 
     * return new FloodStationsResponseDTO(households);
     * }
     */

    public Person getPersonsByStations(List<Integer> stations) {
        logger.debug("[SERVICE] looking for perons covered by stations={}", stations);

        // Récupére toutes les adresses couvertes par les stations
        Set<String> addresses = firestationRepository.findAddressesByStations(stations);

        List<List<Person>> persons = new ArrayList<>();
        // Pour chaque adresse, créer une liste des personnes
        for (String address : addresses) {

            persons.add(personRepository.findByAddress(address));

        }

        return null;
    }

    /**
     * Récupère la liste de toutes les personnes avec le nom de famille donné, leur
     * historique médical et le numéro de la caserne qui les couvrent
     * 
     * @param lastName Nom de famille
     * @return Liste des personnes et de leur historique médical
     */
    /*
     * public PersonInfoResponseDTO getPersonInfosAndMedicalHistoryByLastName(String
     * lastName) {
     * logger.
     * debug("[SERVICE] looking for perons infos and medical history for lastname={}"
     * , lastName);
     * 
     * List<PersonMedicalProfileDTO> personInfolastNameDTOs = new ArrayList<>();
     * 
     * // Récupérer toutes les personnes avec ce nom de famille
     * List<Person> personsAtAddress = personRepository.findByLastName(lastName);
     * 
     * for (Person person : personsAtAddress) {
     * 
     * // Récupérer le MedicalRecord
     * Optional<MedicalRecord> medicalRecordOpt = medicalRecordRepository
     * .findByFirstNameAndLastName(person.getFirstName(), person.getLastName());
     * // Extraire les informations du medical record
     * List<String> medications = medicalRecordOpt
     * .map(MedicalRecord::getMedications)
     * .orElse(Collections.emptyList());
     * 
     * List<String> allergies = medicalRecordOpt
     * .map(MedicalRecord::getAllergies)
     * .orElse(Collections.emptyList());
     * 
     * int age = medicalRecordOpt.isPresent()
     * ? utils.calculateAge(person)
     * : -1;
     * 
     * // Créer le DTO de l'historique médical
     * MedicalHistoryDTO medicalHistory = new MedicalHistoryDTO(
     * medications,
     * allergies);
     * 
     * PersonMedicalProfileDTO personInfolastNameDTO = new PersonMedicalProfileDTO(
     * person.getLastName(),
     * person.getAddress(),
     * age, person.getEmail(),
     * medicalHistory);
     * 
     * personInfolastNameDTOs.add(personInfolastNameDTO);
     * }
     * 
     * return new PersonInfoResponseDTO(personInfolastNameDTOs);
     * }
     */

    /**
     * @param city Ville
     * @return Set des adresse mails des personnes habitant dans cette ville
     */
    /*
     * public CommunityEmailResponseDTO getEmailsaddressesForCityResidents(String
     * city) {
     * 
     * logger.debug("[SERVICE] looking for emails of resident in city={}", city);
     * private communityEmails = new LinkedHashSet<>;
     * // Récupérer toutes les personnes de cette ville
     * return new
     * CommunityEmailResponseDTO(personRepository.findEmailsByCity(city));
     * }
     */
    public List<Person> getPersonsByLastName(String lastName) {
        return personRepository.findByLastName(lastName);
    }

    public List<Person> getPersonsByAddress(String address) {
        return personRepository.findByAddress(address);
    }

    public Set<String> getEmailsByCity(String city) {
        return personRepository.findEmailsByCity(city);
    }

    /**
     * Ajoute une nouvelle personne
     * 
     * @param person La personne à ajouter
     * @throws IllegalArgumentException si la personne existe déjà
     */
    public void addPerson(Person person) {

        if (personRepository.personExists(person.getFirstName(), person.getLastName())) {
            logger.error("[SERVICE] Person already exist: {} {}",
                    person.getFirstName(), person.getLastName());
            throw new IllegalArgumentException("Person already exist");
        }

        personRepository.addPerson(person);
    }

    /**
     * Met à jour une personne existante
     * 
     * @param person Les nouvelles données de la personne
     * @throws IllegalArgumentException si la personne n'existe pas
     */
    public void updatePerson(Person person) {
        if (!personRepository.personExists(person.getFirstName(), person.getLastName())) {
            logger.error("[SERVICE] Person not found: {} {}",
                    person.getFirstName(), person.getLastName());
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.updatePerson(person);
    }

    /**
     * Supprime une personne
     * 
     * @param firstName Prénom
     * @param lastName  Nom
     * @throws IllegalArgumentException si la personne n'existe pas
     */
    public void deletePerson(String firstName, String lastName) {
        if (!personRepository.personExists(firstName, lastName)) {
            logger.error("[SERVICE] Person not found: {} {}",
                    firstName, lastName);
            throw new IllegalArgumentException("Person not found");
        }
        personRepository.deletePerson(firstName, lastName);
    }

}