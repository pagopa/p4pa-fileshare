package it.gov.pagopa.pu.fileshare.connector.organization.config;

import it.gov.pagopa.pu.fileshare.config.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.organization")
@SuperBuilder
@NoArgsConstructor
public class OrganizationApiClientConfig extends ApiClientConfig {
}
