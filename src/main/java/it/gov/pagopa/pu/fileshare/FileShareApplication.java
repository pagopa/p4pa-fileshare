package it.gov.pagopa.pu.fileshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
public class FileShareApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileShareApplication.class, args);
	}

}
