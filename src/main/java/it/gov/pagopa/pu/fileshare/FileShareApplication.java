package it.gov.pagopa.pu.fileshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FileShareApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileShareApplication.class, args);
	}

}
