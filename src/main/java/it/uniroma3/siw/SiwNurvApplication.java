package it.uniroma3.siw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; //

@SpringBootApplication

@EnableAsync(proxyTargetClass = true) //lo uso per poter gestire lettura e scrittura di messaggi del bot dalla sola classe
									// TelegramService piuttosto che dividere in due classi
public class SiwNurvApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiwNurvApplication.class, args);
	}

}
