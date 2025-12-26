package com.openclassrooms.safetynetalerts.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.json.JsonMapper;

/**
 * Implémentation du repository de données utilisant un fichier JSON comme source.
 *
 * <p>Cette classe implémente l'interface {@link DataRepo} et charge les données
 * depuis un fichier JSON nommé "data.json" situé dans le classpath (src/main/resources).
 *
 * <p>Fonctionnement :
 * <ul>
 * <li>Au démarrage de l'application, la méthode {@link #init()} est appelée automatiquement
 *     grâce à l'annotation {@link PostConstruct}</li>
 * <li>Le fichier JSON est désérialisé en objet {@link DataFile} grâce à Jackson</li>
 * <li>Les données sont conservées en mémoire pour un accès rapide</li>
 * <li>Toutes les modifications ultérieures sont faites directement en mémoire (pas de persistence)</li>
 * </ul>
 *
 * <p><strong>Important :</strong> Cette implémentation ne persiste pas les modifications.
 * Tous les changements (ajouts, modifications, suppressions) sont perdus au redémarrage
 * de l'application.
 *
 * @author SafetyNet Alerts
 * @version 1.0
 */
@Repository
public class JsonDataRepo implements DataRepo {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private JsonMapper mapper;

    private DataFile dataFile;

    /**
     * Initialise le repository en chargeant le fichier JSON au démarrage de l'application.
     *
     * <p>Cette méthode est appelée automatiquement après l'injection de dépendances
     * grâce à l'annotation {@link PostConstruct}.
     *
     * <p>Le fichier "data.json" doit être présent dans src/main/resources.
     * S'il n'est pas trouvé ou s'il est mal formaté, une {@link RuntimeException}
     * est levée, empêchant le démarrage de l'application.
     *
     * @throws RuntimeException si le fichier data.json est introuvable ou invalide
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data.json");
            this.dataFile = mapper.readValue(resource.getInputStream(), DataFile.class);
            logger.debug("[REPOSITORY] Json chargé");
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire data.json", e);
        }
    }

    /**
     * Retourne les données chargées depuis le fichier JSON.
     *
     * <p>Cette méthode retourne toujours la même instance de {@link DataFile}
     * chargée au démarrage de l'application. Toutes les modifications effectuées
     * sur cet objet sont donc conservées en mémoire jusqu'au redémarrage.
     *
     * @return l'objet {@link DataFile} contenant toutes les données de l'application
     */
    @Override
    public DataFile loadData() {
        return dataFile;
    }

}
