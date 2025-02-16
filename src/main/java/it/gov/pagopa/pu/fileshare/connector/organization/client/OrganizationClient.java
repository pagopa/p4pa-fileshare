package it.gov.pagopa.pu.fileshare.connector.organization.client;

import it.gov.pagopa.pu.fileshare.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class OrganizationClient {
  private final OrganizationApisHolder organizationApisHolder;

  public OrganizationClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Organization getOrganizationById(Long organizationId, String accessToken) {
    try {
      return organizationApisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization(String.valueOf(organizationId));
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Organization with organization id {} not found", organizationId);
      return null;
    }
  }
}
