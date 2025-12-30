package com.openclassrooms.safetynetalerts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.safetynetalerts.dto.firestation.FirestationCoverageResponseDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationDTO;
import com.openclassrooms.safetynetalerts.dto.firestation.FirestationResidentDTO;
import com.openclassrooms.safetynetalerts.mapper.FirestationMapper;
import com.openclassrooms.safetynetalerts.model.Firestation;
import com.openclassrooms.safetynetalerts.model.Person;
import com.openclassrooms.safetynetalerts.service.FirestationService;
import com.openclassrooms.safetynetalerts.utils.Utils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrôleur REST pour la gestion des casernes de pompiers.
 * <p>
 * Ce contrôleur expose les endpoints CRUD pour gérer les mappings entre
 * les adresses et les numéros de stations de pompiers :
 * <ul>
 * <li>POST /firestation - Création d'un nouveau mapping caserne/adresse</li>
 * <li>PUT /firestation - Mise à jour du numéro de station pour une adresse</li>
 * <li>DELETE /firestation - Suppression d'un mapping par adresse ou numéro de
 * station</li>
 * </ul>
 *
 */
@RestController
@RequestMapping("/firestation")
@Validated
public class FirestationController {

    private final Utils utils;
    private final Logger logger = LoggerFactory.getLogger(FirestationController.class);

    public final FirestationService firestationService;
    public final FirestationMapper firestationMapper;

    public FirestationController(FirestationService firestationService, FirestationMapper firestationMapper,
            Utils utils) {
        this.firestationService = firestationService;
        this.firestationMapper = firestationMapper;
        this.utils = utils;
    }

    /**
     * Récupère la liste des personnes couvertes par une station de pompiers donnée.
     * <p>
     * Endpoint : GET /firestation?stationNumber={stationNumber}
     * <p>
     * Retourne la liste des résidents couverts par la station spécifiée, ainsi
     * qu'un
     * décompte du nombre d'adultes et d'enfants. Cette information est utilisée
     * pour
     * les opérations d'urgence nécessitant une évaluation rapide des populations à
     * risque.
     *
     * @param stationNumber le numéro de la station de pompiers dont on veut
     *                      récupérer les personnes couvertes
     * @return ResponseEntity contenant un {@link FirestationCoverageResponseDTO}
     *         avec la liste des résidents,
     *         le nombre d'adultes et le nombre d'enfants (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<FirestationCoverageResponseDTO> getPersonsByStation(
            @RequestParam("stationNumber") int stationNumber) {

        logger.info("[CALL] GET /firestation?stationNumber={}", stationNumber);

        // 1. Appeler le service pour récupérer les personnes
        List<Person> persons = firestationService.getPersonsCoveredByStation(stationNumber);

        // 2. Calculer les comptages adultes/enfants
        int adultCount = 0;
        int childCount = 0;

        for (Person person : persons) {
            if (utils.isChild(person)) {
                childCount++;
            } else {
                adultCount++;
            }
        }

        // 3. Mapper les entités vers DTOs
        List<FirestationResidentDTO> residents = persons.stream()
                .map(p -> new FirestationResidentDTO(
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAddress(),
                        p.getPhone()))
                .toList();

        // 4. Construire le DTO de réponse
        FirestationCoverageResponseDTO response = new FirestationCoverageResponseDTO(
                residents,
                adultCount,
                childCount);

        logger.info("[RESPONSE] GET /firestation -> {} résidents ({} adultes, {} enfants)",
                residents.size(), adultCount, childCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Crée un nouveau mapping entre une adresse et un numéro de station de
     * pompiers.
     * <p>
     * Endpoint : POST /firestation
     * <p>
     * Permet d'ajouter une nouvelle association entre une adresse et une station de
     * pompiers.
     * Si un mapping existe déjà pour cette adresse, une exception sera levée.
     *
     * @param firestationDTO le DTO contenant l'adresse et le numéro de station à
     *                       associer
     * @return ResponseEntity contenant le {@link FirestationDTO} créé (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<FirestationDTO> addMapping(@Valid @RequestBody FirestationDTO firestationDTO) {
        logger.info("[CALL] POST /firestation -> Adding mapping address={}, station={}",
                firestationDTO.getAddress(), firestationDTO.getStation());

        // Mapper DTO vers Entité
        Firestation firestation = firestationMapper.toEntity(firestationDTO);

        // Appeler le service
        firestationService.addMapping(firestation);

        // Mapper DTO pour la réponse
        FirestationDTO response = firestationMapper.toDto(firestation);

        logger.info("[RESPONSE] POST /firestation -> Mapping created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour le numéro de station de pompiers associé à une adresse.
     * <p>
     * Endpoint : PUT /firestation?address={address}
     * <p>
     * Permet de modifier le numéro de station pour une adresse existante.
     * Si l'adresse n'existe pas dans la base, une exception sera levée.
     *
     * @param address        l'adresse dont on souhaite modifier le numéro de
     *                       station
     * @param firestationDTO le DTO contenant le nouveau numéro de station
     * @return ResponseEntity contenant le {@link FirestationDTO} mis à jour (HTTP
     *         200)
     */
    @PutMapping
    public ResponseEntity<FirestationDTO> updateStation(
            @RequestParam("address") @NotBlank(message = "address is required") String address,
            @RequestBody FirestationDTO firestationDTO) {
        logger.info("[CALL] PUT /firestation -> Updating mapping address={}, new station number={}",
                address, firestationDTO.getStation());

        // Mapper DTO vers Entité
        Firestation firestation = firestationMapper.toEntity(firestationDTO);

        // Appeler le service
        firestationService.updateMapping(address, firestation);

        // Mapper Entité vers DTO pour la réponse
        FirestationDTO response = firestationMapper.toDto(firestation);

        logger.info("[RESPONSE] PUT /firestation -> Mapping updated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un ou plusieurs mappings entre adresse et station de pompiers.
     * <p>
     * Endpoint : DELETE /firestation?address={address} OU DELETE
     * /firestation?station={station}
     * <p>
     * Permet de supprimer un mapping de deux manières :
     * <ul>
     * <li>Par adresse : supprime le mapping spécifique pour cette adresse</li>
     * <li>Par numéro de station : supprime tous les mappings associés à ce
     * numéro</li>
     * </ul>
     * Au moins un des deux paramètres doit être fourni.
     *
     * @param address l'adresse du mapping à supprimer (optionnel)
     * @param station le numéro de station dont on veut supprimer tous les mappings
     *                (optionnel)
     * @return ResponseEntity sans contenu (HTTP 204) en cas de suppression réussie
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMapping(
            @RequestParam(name = "address", required = false) String address,
            @RequestParam(name = "station", required = false) Integer station) {

        logger.info("[CALL] DELETE /firestation -> address={}, station={}", address, station);

        // Appeler le service
        firestationService.deleteMapping(address, station);

        logger.info("[RESPONSE] DELETE /firestation -> Mapping deleted successfully");
        return ResponseEntity.noContent().build();
    }

}