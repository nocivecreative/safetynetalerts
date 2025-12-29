package com.openclassrooms.safetynetalerts.repository;

import com.openclassrooms.safetynetalerts.model.DataFile;

/**
 * Interface définissant le contrat pour le chargement des données de
 * l'application.
 *
 * <p>
 * Cette interface permet d'abstraire la source des données, permettant ainsi
 * de charger les données depuis différentes sources (JSON, base de données,
 * API, etc.)
 * sans modifier le code métier.
 *
 * <p>
 * L'implémentation par défaut {@link JsonDataRepo} charge les données depuis
 * un fichier JSON présent dans le classpath.
 *
 */
public interface DataRepo {
    /**
     * Charge et retourne l'ensemble des données de l'application.
     *
     * <p>
     * Cette méthode doit charger toutes les données nécessaires au fonctionnement
     * de l'application :
     * <ul>
     * <li>La liste des personnes</li>
     * <li>La liste des casernes de pompiers et leurs mappings d'adresses</li>
     * <li>La liste des dossiers médicaux</li>
     * </ul>
     *
     * @return un objet {@link DataFile} contenant toutes les données de
     *         l'application
     */
    DataFile loadData();

}
