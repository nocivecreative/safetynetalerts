package com.openclassrooms.safetynetalerts.mapper;

import org.springframework.stereotype.Component;

import com.openclassrooms.safetynetalerts.dto.firestation.FirestationDTO;
import com.openclassrooms.safetynetalerts.model.Firestation;

/**
 * Mapper pour la conversion entre Firestation et FirestationDTO.
 * Respecte le principe SRP (Single Responsibility Principle).
 */
@Component
public class FirestationMapper {

    /**
     * Convertit une entité Firestation en DTO.
     *
     * @param firestation l'entité à convertir
     * @return le DTO correspondant
     */
    public FirestationDTO toDto(Firestation firestation) {
        if (firestation == null) {
            return null;
        }

        return new FirestationDTO(
            firestation.getAddress(),
            firestation.getStation()
        );
    }

    /**
     * Convertit un DTO FirestationDTO en entité Firestation.
     *
     * @param dto le DTO à convertir
     * @return l'entité correspondante
     */
    public Firestation toEntity(FirestationDTO dto) {
        if (dto == null) {
            return null;
        }

        Firestation firestation = new Firestation();
        firestation.setAddress(dto.getAddress());
        firestation.setStation(dto.getStation());

        return firestation;
    }
}
