package com.openclassrooms.safetynetalerts.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.safetynetalerts.model.MedicalRecord;
import com.openclassrooms.safetynetalerts.repository.MedicalRecordRepository;

/**
 * Service de gestion des dossiers médicaux.
 *
 * <p>
 * Ce service fournit les opérations CRUD pour les dossiers médicaux, incluant :
 * <ul>
 * <li>Création de nouveaux dossiers médicaux</li>
 * <li>Mise à jour des informations médicales (médicaments, allergies, date de
 * naissance)</li>
 * <li>Suppression de dossiers médicaux</li>
 * <li>Récupération de dossiers médicaux</li>
 * </ul>
 *
 * <p>
 * Chaque dossier médical est identifié de manière unique par la combinaison
 * prénom + nom de famille, et contient :
 * <ul>
 * <li>La date de naissance (utilisée pour calculer l'âge)</li>
 * <li>La liste des médicaments prescrits</li>
 * <li>La liste des allergies connues</li>
 * </ul>
 *
 * <p>
 * Ces informations sont essentielles pour les services d'urgence lors
 * d'interventions.
 *
 */
@Service
public class MedicalRecordService {
    private final Logger logger = LoggerFactory.getLogger(MedicalRecordService.class);

    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * Récupère un dossier médical par prénom et nom de famille.
     *
     * <p>
     * Cette méthode recherche un dossier médical existant dans le système.
     *
     * @param firstName le prénom de la personne dont on veut récupérer le dossier
     *                  médical
     * @param lastName  le nom de famille de la personne dont on veut récupérer le
     *                  dossier médical
     * @return un {@link Optional} contenant le dossier médical si trouvé, sinon
     *         {@link Optional#empty()}
     */
    public Optional<MedicalRecord> getMedicalRecord(String firstName, String lastName) {
        return medicalRecordRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    /**
     * Crée un nouveau dossier médical dans le système.
     *
     * <p>
     * Cette méthode vérifie d'abord qu'aucun dossier médical n'existe déjà pour
     * la personne identifiée par le prénom et le nom de famille fournis.
     *
     * <p>
     * Le dossier médical doit contenir :
     * <ul>
     * <li>Le prénom et le nom de famille (identifiants uniques)</li>
     * <li>La date de naissance au format MM/dd/yyyy</li>
     * <li>La liste des médicaments (peut être vide)</li>
     * <li>La liste des allergies (peut être vide)</li>
     * </ul>
     *
     * <p>
     * Si un dossier médical existe déjà pour cette personne, une exception est
     * levée.
     *
     * @param medicalRecord le dossier médical à créer
     * @return le dossier médical créé
     * @throws IllegalArgumentException si un dossier médical existe déjà pour cette
     *                                  personne
     */
    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        logger.info("Creating medical record: {} {}",
                medicalRecord.getFirstName(), medicalRecord.getLastName());

        // Vérifier si le dossier existe déjà
        if (medicalRecordRepository.existsByFirstNameAndLastName(
                medicalRecord.getFirstName(),
                medicalRecord.getLastName())) {
            logger.error("Medical record already exists: {} {}",
                    medicalRecord.getFirstName(), medicalRecord.getLastName());
            throw new IllegalArgumentException(
                    "Medical record for " + medicalRecord.getFirstName() + " "
                            + medicalRecord.getLastName() + " already exists");
        }

        // Sauvegarder
        MedicalRecord savedRecord = medicalRecordRepository.save(medicalRecord);

        logger.info("Medical record created successfully: {} {}",
                savedRecord.getFirstName(), savedRecord.getLastName());

        return savedRecord;
    }

    /**
     * Met à jour un dossier médical existant.
     *
     * <p>
     * Cette méthode recherche le dossier médical par prénom et nom, puis met à jour
     * ses informations.
     * La mise à jour partielle est supportée : seuls les champs non-null de
     * {@code medicalRecord} sont appliqués.
     *
     * <p>
     * Si aucun dossier médical n'existe pour cette personne, une exception est
     * levée.
     *
     * @param firstName   le prénom de la personne dont on veut mettre à jour le
     *                    dossier
     * @param lastName    le nom de famille de la personne dont on veut mettre à
     *                    jour le dossier
     * @param updatedData les nouvelles informations médicales (seuls les champs
     *                    non-null sont mis à jour)
     * @return le dossier médical mis à jour
     * @throws IllegalArgumentException si aucun dossier médical n'existe pour cette
     *                                  personne
     */
    public MedicalRecord updateMedicalRecord(String firstName, String lastName,
            MedicalRecord updated) {

        logger.info("[SERVICE] Updating medical record: {} {}", firstName, lastName);

        MedicalRecord existing = medicalRecordRepository
                .findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medical record for " + firstName + " " + lastName + " not found"));

        medicalRecordRepository.updateFields(existing, updated);
        return existing;
    }

    /**
     * Supprime un dossier médical du système.
     *
     * <p>
     * Cette méthode recherche et supprime le dossier médical identifié par
     * la combinaison prénom + nom de famille.
     *
     * <p>
     * <strong>Attention :</strong> Cette suppression n'affecte PAS la personne
     * elle-même
     * dans le système. Seules les informations médicales sont supprimées.
     * Après cette opération, les endpoints qui retournent l'âge de la personne
     * retourneront -1 (car la date de naissance n'est plus disponible).
     *
     * <p>
     * Si aucun dossier médical n'existe pour cette personne, une exception est
     * levée.
     *
     * @param firstName le prénom de la personne dont on veut supprimer le dossier
     *                  médical
     * @param lastName  le nom de famille de la personne dont on veut supprimer le
     *                  dossier médical
     * @throws IllegalArgumentException si aucun dossier médical n'existe pour cette
     *                                  personne
     */
    public void deleteMedicalRecord(String firstName, String lastName) {
        logger.info("[SERVICE] Deleting medical record: {} {}", firstName, lastName);

        if (!medicalRecordRepository.existsByFirstNameAndLastName(firstName, lastName)) {
            throw new IllegalArgumentException(
                    "Medical record for " + firstName + " " + lastName + " not found");
        }

        if (!medicalRecordRepository.delete(firstName, lastName))
            throw new IllegalArgumentException("Erreur durant la suppression");
    }
}