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

@Repository
public class FirestationRepository {

    @Autowired
    private DataRepo dataRepo;

    private DataFile data;

    @PostConstruct // Post injection de dépendances
    public void init() {
        this.data = dataRepo.loadData();
    }

    public List<Firestation> findAll() {
        return data.getFirestations().stream()
                .toList();
    }

    public List<Firestation> findByStation(Integer stationNumber) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getStation() == (stationNumber))
                .toList();
    }

    public List<String> findAddressesByStation(Integer stationNumber) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getStation() == (stationNumber))
                .map(Firestation::getAddress)
                .toList();
    }

    public boolean existsByStation(Integer stationNumber) {
        return data.getFirestations().stream()
                .anyMatch(fs -> fs.getStation() == (stationNumber));
    }

    public boolean existsByAddress(String address) {
        return data.getFirestations().stream()
                .anyMatch(fs -> fs.getAddress().equals(address));
    }

    // Optional<integer> et pas int avec .orElse(null) car null n'existe pas pour
    // int, donc npe
    public Optional<Integer> findStationByAddress(String address) {
        return data.getFirestations().stream()
                .filter(fs -> fs.getAddress().equals(address))
                .map(Firestation::getStation)
                .findFirst();
    }

    public Set<String> findAddressesByStations(List<Integer> stations) {
        return data.getFirestations().stream()
            .filter(fs -> stations.contains(fs.getStation()))
            .map(Firestation::getAddress)
            .collect(Collectors.toSet());
    }
    
    public void addFirestation(Firestation firestation) {
        data.getFirestations().add(firestation);
    }

    public boolean firestationAddressExists(String address) {
        return data.getFirestations().stream()
                .anyMatch(f -> f.getAddress().equals(address));
    }

    public void updateFirestation(Firestation firestation) { // TODO check à faire !!
        deleteFirestationByStation(firestation.getStation());
        addFirestation(firestation);
    }

    public void deleteFirestationByAddress(String address) {
        data.getFirestations().removeIf(p -> p.getAddress().equals(address));
    }

    public void deleteFirestationByStation(int station) {
        data.getFirestations().removeIf(p -> p.getStation() == (station));
    }
}
