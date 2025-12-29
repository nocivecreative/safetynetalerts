package com.openclassrooms.safetynetalerts.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour fournir un bean Clock.
 * Permet l'injection d'un Clock dans les composants qui nécessitent
 * des opérations basées sur le temps, facilitant ainsi les tests.
 */
@Configuration
public class ClockConfig {

    /**
     * Fournit un Clock système par défaut.
     * Ce bean peut être surchargé dans les tests pour utiliser un Clock fixe.
     *
     * @return Clock configuré avec le fuseau horaire système par défaut
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
