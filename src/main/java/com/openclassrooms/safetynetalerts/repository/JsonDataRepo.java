package com.openclassrooms.safetynetalerts.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonDataRepo implements DataRepo {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private JsonMapper mapper;

    private DataFile dataFile;

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

    @Override
    public DataFile loadData() {
        return dataFile;
    }

}
