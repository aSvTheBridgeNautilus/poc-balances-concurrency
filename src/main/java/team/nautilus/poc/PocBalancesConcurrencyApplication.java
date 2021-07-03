package team.nautilus.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class PocBalancesConcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocBalancesConcurrencyApplication.class, args);
	}

}
