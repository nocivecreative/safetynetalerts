package com.openclassrooms.safetynetalerts.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.openclassrooms.safetynetalerts.model.DataFile;

import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonDataRepo implements DataRepo {
    private final Logger logger = LoggerFactory.getLogger(JsonDataRepo.class);

    @Autowired
    private JsonMapper mapper;

    @Override
    public DataFile loadData() {
        try {
            ClassPathResource resource = new ClassPathResource("data.json");
            DataFile Jsondata = mapper.readValue(resource.getInputStream(), DataFile.class);
            logger.debug("[REPOSITORY] Json file read and parsed");
            return Jsondata;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de lire data.json", e);
        }
    }

}
