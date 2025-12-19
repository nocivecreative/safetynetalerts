package com.openclassrooms.safetynetalerts.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class JsonDataRepoTest {

    @Autowired
    private JsonDataRepo dataRepo;

    @Test
    void shouldGetAllPerson() {

    }
}
