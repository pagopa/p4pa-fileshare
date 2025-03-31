package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.auth.client.AuthnClient;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class AuthorizationService {
  public static final String ROLE_ADMIN = "ROLE_ADMIN";

  private final AuthnClient authnClient;

  public AuthorizationService(AuthnClient authnClient) {
    this.authnClient = authnClient;
  }

  public UserInfo validateToken(String accessToken){
    log.info("Requesting validate token");
    return authnClient.getUserInfo(accessToken);
  }

  public static boolean isAdminRole(Long organizationId, UserInfo loggedUser) {
    return getUserOrganizationRoles(organizationId, loggedUser)
      .filter(o -> !CollectionUtils.isEmpty(o.getRoles()) && o.getRoles()
        .contains(ROLE_ADMIN))
      .isPresent();
  }

  private static Optional<UserOrganizationRoles> getUserOrganizationRoles(Long organizationId, UserInfo loggedUser) {
    return loggedUser.getOrganizations().stream()
      .filter(o -> organizationId.equals(o.getOrganizationId()) && !CollectionUtils.isEmpty(o.getRoles()))
      .findFirst();
  }
}
