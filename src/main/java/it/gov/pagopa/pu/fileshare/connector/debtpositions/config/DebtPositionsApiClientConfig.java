package it.gov.pagopa.pu.fileshare.connector.debtpositions.config;

import it.gov.pagopa.pu.fileshare.config.rest.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.debt-positions")
@SuperBuilder
@NoArgsConstructor
public class DebtPositionsApiClientConfig extends ApiClientConfig {
}
