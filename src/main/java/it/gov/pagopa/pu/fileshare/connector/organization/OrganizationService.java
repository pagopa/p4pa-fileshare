package it.gov.pagopa.pu.fileshare.connector.organization;

import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;

public interface OrganizationService {
  Organization getOrganizationById(Long organizationId, String accessToken);
}
