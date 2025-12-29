package com.openclassrooms.safetynetalerts.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.model.Firestation;

import jakarta.annotation.PostConstruct;

/**
 * Repository de gestion des mappings entre casernes de pompiers et adresses.
 *
 * <p>
 * Ce repository fournit l'accès aux données des casernes stockées dans le
 * fichier JSON,
 * avec les opérations suivantes :
 * <ul>
 * <li>Recherche de casernes par numéro ou par adresse</li>
 * <li>Récupération des adresses couvertes par une ou plusieurs casernes</li>
 * <li>Vérification d'existence de mappings</li>
 * <li>Opérations CRUD (Create, Read, Update, Delete)</li>
 * </ul>
 *
 * <p>
 * Le système de mapping permet d'associer plusieurs adresses à une même
 * caserne,
 * définissant ainsi les zones de couverture géographique pour les interventions
 * d'urgence.
 * Chaque adresse ne peut être couverte que par une seule caserne.
 *
 * <p>
 * <b>Note sur la thread-safety :</b> Ce repository n'est pas thread-safe. Les
 * opérations
 * de modification (add, update, delete) affectent directement la liste en
 * mémoire sans
 * synchronisation. Dans un environnement multi-threadé, des mécanismes de
 * synchronisation
 * externes doivent être mis en place au niveau de la couche service.
 *
 */
@Repository
public class FirestationRepository {

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
    @PostConstruct // Post injection de dépendances
    public void init() {
        this.data = dataRepo.loadData();
    }

    /**
     * Récupère la liste complète de tous les mappings caserne/adresse.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'une nouvelle liste via {@code toList()} garantit l'isolation
     * des données retournées.
     *
     * @return la liste de tous les mappings, ou une liste vide si aucun mapping
     *         n'existe
     */
    public List<Firestation> findAll() {
        return data.getFirestations().stream()
                .toList();
    }

    /**
     * Recherche tous les mappings associés à un numéro de caserne donné.
     *
     * <p>
     * Une caserne peut couvrir plusieurs adresses, cette méthode retourne
     * tous les mappings pour une caserne donnée.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'une nouvelle liste via {@code toList()} garantit l'isolation
     * des données retournées.
     *
     * @param stationNumber le numéro de la caserne à rechercher
     * @return la liste des mappings pour cette caserne, ou une liste vide si aucun
     *         mapping trouvé
     */
    public List<Firestation> findByStation(Integer stationNumber) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getStation() == (stationNumber))
                .toList();
    }

    /**
     * Récupère toutes les adresses couvertes par une caserne donnée.
     *
     * <p>
     * Cette méthode est utilisée pour déterminer la zone de couverture géographique
     * d'une caserne lors d'une intervention d'urgence.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'une nouvelle liste via {@code toList()} garantit l'isolation
     * des données retournées.
     *
     * @param stationNumber le numéro de la caserne
     * @return la liste des adresses couvertes par cette caserne, ou une liste vide
     *         si aucune
     */
    public List<String> findAddressesByStation(Integer stationNumber) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getStation() == (stationNumber))
                .map(Firestation::getAddress)
                .toList();
    }

    /**
     * Vérifie si un numéro de caserne existe dans le système.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param stationNumber le numéro de caserne à vérifier
     * @return {@code true} si au moins un mapping existe pour ce numéro,
     *         {@code false} sinon
     */
    public boolean existsByStation(Integer stationNumber) {
        return data.getFirestations().stream()
                .anyMatch(fs -> fs.getStation() == (stationNumber));
    }

    /**
     * Vérifie si une adresse est déjà couverte par une caserne.
     *
     * <p>
     * Chaque adresse ne peut être couverte que par une seule caserne.
     * Cette méthode permet de vérifier si une adresse a déjà un mapping.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param address l'adresse à vérifier
     * @return {@code true} si l'adresse est déjà couverte, {@code false} sinon
     */
    public boolean existsByAddress(String address) {
        return data.getFirestations().stream()
                .anyMatch(fs -> fs.getAddress().equals(address));
    }

    /**
     * Recherche le numéro de caserne qui couvre une adresse donnée.
     *
     * <p>
     * Cette méthode retourne un {@link Optional} car une adresse peut ne pas
     * être couverte par une caserne. L'utilisation d'Optional évite les
     * NullPointerException
     * lors de la manipulation de types primitifs (int ne peut pas être null).
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param address l'adresse à rechercher
     * @return un {@link Optional} contenant le numéro de caserne si trouvé, sinon
     *         {@link Optional#empty()}
     */
    public Optional<Integer> findStationNumberByAddress(String address) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getAddress().equals(address))
                .map(Firestation::getStation)
                .findFirst();
    }

    /**
     * Recherche le mapping caserne/adresse complet pour une adresse donnée.
     *
     * <p>
     * Cette méthode retourne l'objet {@link Firestation} complet, contrairement à
     * {@link #findStationNumberByAddress(String)} qui ne retourne que le numéro.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     *
     * @param address l'adresse à rechercher
     * @return un {@link Optional} contenant le mapping complet si trouvé, sinon
     *         {@link Optional#empty()}
     */
    public Optional<Firestation> findStationByAddress(String address) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getAddress().equals(address))
                .findFirst();
    }

    /**
     * Récupère l'ensemble des adresses uniques couvertes par plusieurs casernes.
     *
     * <p>
     * Cette méthode est utilisée pour le endpoint /flood qui retourne les foyers
     * couverts par une liste de casernes. L'utilisation d'un {@link Set} garantit
     * l'unicité des adresses (même si en pratique une adresse ne devrait être
     * couverte que par une seule caserne).
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode est thread-safe en lecture seule.
     * La création d'un nouveau Set via {@code collect()} garantit l'isolation
     * des données retournées.
     *
     * @param stations la liste des numéros de casernes
     * @return un ensemble d'adresses uniques couvertes par ces casernes
     */
    public Set<String> findAddressesByStations(List<Integer> stations) {
        return data.getFirestations().stream()
                .filter(fs -> stations.contains(fs.getStation()))
                .map(Firestation::getAddress)
                .collect(Collectors.toSet());
    }

    /**
     * Ajoute un nouveau mapping caserne/adresse au système.
     *
     * <p>
     * Cette méthode ajoute directement le mapping à la liste en mémoire.
     * Aucune validation n'est effectuée à ce niveau - c'est la responsabilité
     * du service appelant de vérifier l'unicité de l'adresse.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param firestation le mapping caserne/adresse à ajouter
     */
    public void addFirestation(Firestation firestation) {
        data.getFirestations().add(firestation);
    }

    /**
     * Met à jour un mapping caserne/adresse existant.
     *
     * <p>
     * Cette méthode recherche le mapping par son adresse et met à jour le numéro
     * de caserne associé. Si aucun mapping n'existe pour cette adresse, aucune
     * modification n'est effectuée.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement l'objet en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param firestation le mapping contenant l'adresse à mettre à jour et le
     *                    nouveau numéro de caserne
     */
    public void updateFirestation(Firestation firestation) {

        findStationByAddress(firestation.getAddress())
                .ifPresent(existingFs -> {
                    existingFs.setStation(firestation.getStation());
                });
    }

    /**
     * Supprime le mapping d'une adresse spécifique.
     *
     * <p>
     * Cette méthode supprime le mapping pour l'adresse spécifiée.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param address l'adresse dont on veut supprimer le mapping
     * @throws IllegalArgumentException si l'adresse spécifiée n'est pas trouvée
     */
    public void deleteFirestationByAddress(String address) {
        if (!data.getFirestations().removeIf(p -> p.getAddress().equals(address))) {
            throw new IllegalArgumentException("Aucune caserne trouvée à cette adresse");
        }
    }

    /**
     * Supprime tous les mappings associés à un numéro de caserne.
     *
     * <p>
     * Cette méthode supprime tous les mappings pour la caserne spécifiée.
     * Une caserne peut couvrir plusieurs adresses, donc cette opération peut
     * supprimer plusieurs mappings en une seule fois.
     *
     * <p>
     * <b>Thread-safety :</b> Cette méthode n'est PAS thread-safe. Elle modifie
     * directement la liste en mémoire sans synchronisation. En environnement
     * concurrent,
     * des mécanismes de synchronisation doivent être mis en place au niveau
     * service.
     *
     * @param station le numéro de caserne dont on veut supprimer tous les mappings
     * @throws IllegalArgumentException si le numéro de caserne spécifié n'existe
     *                                  pas
     */
    public void deleteFirestationByStation(int station) {
        if (!data.getFirestations().removeIf(p -> p.getStation() == (station))) {
            throw new IllegalArgumentException("Le numéro de caserne indiqué n'existe pas");
        }
    }
}
