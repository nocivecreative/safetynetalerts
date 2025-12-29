package com.openclassrooms.safetynetalerts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.MedicalRecord;

import jakarta.annotation.PostConstruct;

/**
 * Repository de gestion des dossiers médicaux.
 *
 * <p>
 * Ce repository fournit l'accès aux données médicales stockées dans le fichier
 * JSON,
 * avec les opérations suivantes :
 * <ul>
 * <li>Recherche de dossiers médicaux par prénom et nom</li>
 * <li>Vérification d'existence de dossiers</li>
 * <li>Opérations CRUD (Create, Read, Update, Delete)</li>
 * <li>Mise à jour partielle (seuls les champs non-null sont modifiés)</li>
 * </ul>
 *
 * <p>
 * Chaque dossier médical est identifié de manière unique par la combinaison
 * prénom + nom de famille, et contient :
 * <ul>
 * <li>La date de naissance (format MM/dd/yyyy)</li>
 * <li>La liste des médicaments prescrits</li>
 * <li>La liste des allergies connues</li>
 * </ul>
 *
 * <p>
 * <b>Note sur la thread-safety :</b> Ce repository n'est pas thread-safe. Les
 * opérations
 * de modification (save, update, delete) affectent directement la liste en
 * mémoire sans
 * synchronisation. Dans un environnement multi-threadé, des mécanismes de
 * synchronisation
 * externes doivent être mis en place au niveau de la couche service.
 *
 */
@Repository
public class MedicalRecordRepository {

    @Autowired
    private DataRepo dataRepo;

    private DataFile data;

    /**
     * Initialise le repository en chargeant les données depuis le fichier JSON.
     *
     * <p>
     * Cette méthode est appelée automatiquement après la construction du bean
     * Spring
     * grâce à l'annotation {@link PostConstruct}. Elle charge toutes les données
     * en mémoire pour un accès rapide.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est appelée une seule fois au démarrage
     * de l'application par le conteneur Spring, avant que le bean ne soit utilisé.
     */
    @PostConstruct
    public void init() {
        this.data = dataRepo.loadData();
    }

    /**
     * Récupère la liste complète de tous les dossiers médicaux.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode retourne la référence directe à la liste
     * en mémoire. Elle n'est pas thread-safe si des modifications concurrentes sont
     * effectuées.
     *
     * @return la liste de tous les dossiers médicaux, ou une liste vide si aucun
     *         dossier n'existe
     */
    public List<MedicalRecord> findAll() {
        return data.getMedicalrecords();
    }

    /**
     * Recherche un dossier médical par prénom et nom de famille.
     *
     * <p>
     * Cette méthode effectue une correspondance exacte sur la combinaison prénom +
     * nom,
     * qui constitue l'identifiant unique d'un dossier médical dans le système.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param firstName le prénom de la personne (sensible à la casse,
     *                  correspondance exacte)
     * @param lastName  le nom de famille de la personne (sensible à la casse,
     *                  correspondance exacte)
     * @return un {@link Optional} contenant le dossier médical si trouvé, sinon
     *         {@link Optional#empty()}
     */
    public Optional<MedicalRecord> findByFirstNameAndLastName(String firstName, String lastName) {
        return data.getMedicalrecords().stream()
                .filter(mr -> mr.getFirstName().equals(firstName)
                        && mr.getLastName().equals(lastName))
                .findFirst();
    }

    /**
     * Vérifie si un dossier médical existe pour une personne donnée.
     *
     * <p>
     * Cette méthode vérifie l'existence d'un dossier par la combinaison unique
     * prénom + nom de famille.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param firstName le prénom de la personne à rechercher
     * @param lastName  le nom de famille de la personne à rechercher
     * @return {@code true} si le dossier médical existe, {@code false} sinon
     */
    public boolean existsByFirstNameAndLastName(String firstName, String lastName) {
        return data.getMedicalrecords().stream()
                .anyMatch(mr -> mr.getFirstName().equals(firstName)
                        && mr.getLastName().equals(lastName));
    }

    /**
     * Sauvegarde un dossier médical (création ou mise à jour automatique).
     *
     * <p>
     * Cette méthode implémente une logique "upsert" :
     * <ul>
     * <li>Si le dossier existe déjà (basé sur prénom + nom), il est mis à jour</li>
     * <li>Sinon, un nouveau dossier est créé</li>
     * </ul>
     *
     * <p>
     * La mise à jour est partielle : seuls les champs non-null du paramètre
     * {@code medicalRecord} sont appliqués au dossier existant.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste ou l'objet en mémoire sans synchronisation. En
     * environnement concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param medicalRecord le dossier médical à sauvegarder
     * @return le dossier médical sauvegardé (existant mis à jour ou nouveau créé)
     */
    public MedicalRecord save(MedicalRecord medicalRecord) {
        // Vérifier si le dossier existe déjà
        Optional<MedicalRecord> existing = findByFirstNameAndLastName(
                medicalRecord.getFirstName(),
                medicalRecord.getLastName());

        if (existing.isPresent()) {
            // Si il existe, on le met à jour
            MedicalRecord existingRecord = existing.get();
            updateMedicalRecordFields(existingRecord, medicalRecord);
            return existingRecord;
        } else {
            // Sinon on l'ajoute
            data.getMedicalrecords().add(medicalRecord);
            return medicalRecord;
        }
    }

    /**
     * Met à jour un dossier médical existant.
     *
     * <p>
     * Cette méthode recherche le dossier par prénom et nom, puis met à jour
     * ses informations médicales. La mise à jour est partielle : seuls les champs
     * non-null de {@code updatedRecord} sont appliqués.
     *
     * <p>
     * Les champs pouvant être mis à jour :
     * <ul>
     * <li>Date de naissance</li>
     * <li>Liste des médicaments</li>
     * <li>Liste des allergies</li>
     * </ul>
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement l'objet en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param firstName     le prénom de la personne dont on veut mettre à jour le
     *                      dossier
     * @param lastName      le nom de famille de la personne dont on veut mettre à
     *                      jour le dossier
     * @param updatedRecord les nouvelles informations médicales (seuls les champs
     *                      non-null sont mis à jour)
     * @return un {@link Optional} contenant le dossier mis à jour si trouvé, sinon
     *         {@link Optional#empty()}
     */
    public Optional<MedicalRecord> update(String firstName, String lastName, MedicalRecord updatedRecord) {
        Optional<MedicalRecord> existing = findByFirstNameAndLastName(firstName, lastName);

        if (existing.isPresent()) {
            MedicalRecord record = existing.get();
            updateMedicalRecordFields(record, updatedRecord);
            return Optional.of(record);
        }

        return Optional.empty();
    }

    /**
     * Supprime un dossier médical du système.
     *
     * <p>
     * Cette méthode supprime le dossier identifié par la combinaison prénom + nom
     * de famille.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param firstName le prénom de la personne dont on veut supprimer le dossier
     * @param lastName  le nom de famille de la personne dont on veut supprimer le
     *                  dossier
     * @return {@code true} si un dossier a été supprimé, {@code false} si aucun
     *         dossier ne correspondait
     */
    public boolean delete(String firstName, String lastName) {
        return data.getMedicalrecords().removeIf(
                mr -> mr.getFirstName().equals(firstName)
                        && mr.getLastName().equals(lastName));
    }

    /**
     * Met à jour les champs d'un dossier médical existant avec les valeurs d'un
     * dossier mis à jour.
     *
     * <p>
     * Cette méthode privée implémente la logique de mise à jour partielle :
     * seuls les champs non-null du dossier {@code updated} sont copiés vers le
     * dossier {@code existing}.
     *
     * <p>
     * Le prénom et le nom de famille ne sont jamais modifiés car ils constituent
     * l'identifiant unique du dossier.
     *
     * @param existing le dossier médical existant à modifier
     * @param updated  le dossier contenant les nouvelles valeurs (seuls les champs
     *                 non-null sont pris en compte)
     */
    private void updateMedicalRecordFields(MedicalRecord existing, MedicalRecord updated) {
        // Le prénom et nom ne changent pas (identifiant unique)
        if (updated.getBirthdate() != null) {
            existing.setBirthdate(updated.getBirthdate());
        }
        if (updated.getMedications() != null) {
            existing.setMedications(updated.getMedications());
        }
        if (updated.getAllergies() != null) {
            existing.setAllergies(updated.getAllergies());
        }
    }

}
