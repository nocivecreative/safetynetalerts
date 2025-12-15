package com.openclassrooms.safetynetalerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.openclassrooms.safetynetalerts.model.DataFile;
import com.openclassrooms.safetynetalerts.repository.JsonDataRepo;

@SpringBootApplication
public class SafetynetalertsApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(SafetynetalertsApplication.class, args);
		JsonDataRepo repo = ctx.getBean(JsonDataRepo.class);
		DataFile data = repo.loadData();
		System.out.println(data);
	}

}
