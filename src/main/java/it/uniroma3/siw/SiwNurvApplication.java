package it.uniroma3.siw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; //

@SpringBootApplication
@EnableAsync //
public class SiwNurvApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiwNurvApplication.class, args);
	}

}
