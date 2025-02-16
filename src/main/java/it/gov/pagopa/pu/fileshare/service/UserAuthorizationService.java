package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.organization.OrganizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class UserAuthorizationService {
  private final OrganizationService organizationService;

  public UserAuthorizationService(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  public void checkUserAuthorization(Long organizationId, UserInfo user, String accessToken) {
    Organization organization = organizationService.getOrganizationById(organizationId, accessToken);
    boolean isAuthorized = user.getOrganizations().stream()
      .anyMatch(o -> o.getOrganizationIpaCode().equals(organization.getIpaCode())
        && !CollectionUtils.isEmpty(o.getRoles()));
    if(!isAuthorized){
      log.debug("Unauthorized user. [organizationId:{}]", organizationId);
      throw new AuthorizationDeniedException("Access Denied");
    }
  }
}
