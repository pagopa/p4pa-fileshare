package it.gov.pagopa.pu.fileshare.connector.organization;

import it.gov.pagopa.pu.fileshare.connector.organization.client.OrganizationClient;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationClient client;

  public OrganizationServiceImpl(OrganizationClient client) {
    this.client = client;
  }

  @Override
  public Organization getOrganizationById(Long organizationId, String accessToken) {
    return client.getOrganizationById(organizationId, accessToken);
  }
}
