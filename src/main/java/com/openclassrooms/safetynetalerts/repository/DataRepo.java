package com.openclassrooms.safetynetalerts.repository;

import com.openclassrooms.safetynetalerts.model.DataFile;

public interface DataRepo {
    DataFile loadData();
}
