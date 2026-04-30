package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.auth.client.AuthnClient;
import it.gov.pagopa.pu.fileshare.connector.organization.OrganizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;

import java.util.Objects;
import java.util.Optional;

import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class AuthorizationService {
  public static final String ROLE_ADMIN = "ROLE_ADMIN";

  private final AuthnClient authnClient;
  private final OrganizationService organizationService;

  public AuthorizationService(AuthnClient authnClient, OrganizationService organizationService) {
    this.authnClient = authnClient;
    this.organizationService = organizationService;
  }

  public UserInfo validateToken(String accessToken) {
    log.info("Requesting validate token");
    return authnClient.getUserInfo(accessToken);
  }

  /** It will validate if the current user is admin for the organizationId or its broker and return orgFiscalCode */
  public String validateAdminRoleOrBrokerAdmin(Long organizationId, UserInfo loggedUser, String accessToken) {
    boolean isAdmin = isAdminRole(organizationId, loggedUser);
    if (!isAdmin) {
      Organization org = organizationService.getOrganizationById(organizationId, accessToken);
      if(org != null && org.getBrokerId() != null && org.getBrokerId().equals(loggedUser.getBrokerId())) {
        validateBrokerAdminRole(loggedUser);
        return org.getOrgFiscalCode();
      } else {
        handleUnauthorizedUser(organizationId, loggedUser);
      }
    }
    return getOrgFiscalCodeFromUserInfo(loggedUser, organizationId);
  }

  public static void validateAdminRole(Long organizationId, UserInfo loggedUser) {
    boolean roleAdmin = isAdminRole(organizationId, loggedUser);
    if (!roleAdmin) {
      handleUnauthorizedUser(organizationId, loggedUser);
    }
  }

  public static void validateBrokerAdminRole(UserInfo loggedUser) {
    String brokerFiscalCode = loggedUser.getBrokerFiscalCode();
    boolean isBroker = loggedUser.getOrganizations()
      .stream()
      .anyMatch(o ->
        Objects.equals(o.getOrganizationFiscalCode(), brokerFiscalCode) &&
          !CollectionUtils.isEmpty(o.getRoles()) &&
          o.getRoles().contains(ROLE_ADMIN));
    if (!isBroker) {
      handleUnauthorizedUserBrokerFiscalCode(brokerFiscalCode, loggedUser);
    }
  }

  public static boolean isAdminRole(Long organizationId, UserInfo loggedUser) {
    return getUserOrganizationRoles(organizationId, loggedUser)
      .filter(o -> !CollectionUtils.isEmpty(o.getRoles()) && o.getRoles()
        .contains(ROLE_ADMIN))
      .isPresent();
  }

  public static boolean isAdminRole(String organizationIpaCode, UserInfo loggedUser) {
    return loggedUser != null && getUserOrganizationRoles(organizationIpaCode, loggedUser)
      .filter(o -> !CollectionUtils.isEmpty(o.getRoles()) && o.getRoles()
        .contains(ROLE_ADMIN))
      .isPresent();
  }

  public static void validateUserForOrganizationId(Long organizationId, UserInfo loggedUser) {
    if (getUserOrganizationRoles(organizationId, loggedUser).isEmpty()) {
      handleUnauthorizedUser(organizationId, loggedUser);
    }
  }

  public static String getOrgIpaCodeFromUserInfo(UserInfo loggedUser, Long organizationId) {
    if (loggedUser == null || organizationId == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationId, loggedUser).map(UserOrganizationRoles::getOrganizationIpaCode)
      .orElse(null);
  }

  public static String getOrgFiscalCodeFromUserInfo(UserInfo loggedUser, Long organizationId) {
    if (loggedUser == null || organizationId == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationId, loggedUser).map(UserOrganizationRoles::getOrganizationFiscalCode)
      .orElse(null);
  }

  public static String getOrgFiscalCodeFromUserInfo(UserInfo loggedUser, String organizationIpaCode) {
    if (loggedUser == null || organizationIpaCode == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationIpaCode, loggedUser).map(UserOrganizationRoles::getOrganizationFiscalCode)
      .orElse(null);
  }

  public static Long getOrganizationIdFromUserInfo(UserInfo loggedUser, String organizationIpaCode) {
    if (loggedUser == null || organizationIpaCode == null) {
      return null;
    }
    return getUserOrganizationRoles(organizationIpaCode, loggedUser).map(UserOrganizationRoles::getOrganizationId)
      .orElse(null);
  }

  public static Long getOrganizationIdFromOrgFiscalCode(UserInfo loggedUser, String organizationFiscalCode) {
    if (loggedUser == null || organizationFiscalCode == null) {
      return null;
    }

    return getUserOrganizationRolesFromOrgFiscalCode(organizationFiscalCode, loggedUser).map(UserOrganizationRoles::getOrganizationId)
      .orElse(null);
  }

  private static void handleUnauthorizedUser(Long organizationId, UserInfo loggedUser) {
    log.debug("Unauthorized user. [organizationId:{}]", organizationId);
    throw new AuthorizationDeniedException("[USER_UNAUTHORIZED] Access denied on organizationId " + organizationId + " to user " + loggedUser.getMappedExternalUserId());
  }

  private static void handleUnauthorizedUserBrokerFiscalCode(String brokerOrgFiscalCode, UserInfo loggedUser) {
    log.debug("Unauthorized user. [brokerOrgFiscalCode:{}]", brokerOrgFiscalCode);
    String userId = loggedUser != null ? loggedUser.getMappedExternalUserId() : "UNKNOWN";
    throw new AuthorizationDeniedException("[USER_UNAUTHORIZED] Access denied on brokerOrgFiscalCode " + brokerOrgFiscalCode + " to user " + userId);
  }

  private static Optional<UserOrganizationRoles> getUserOrganizationRoles(Long organizationId, UserInfo loggedUser) {
    return loggedUser.getOrganizations().stream()
      .filter(o -> organizationId.equals(o.getOrganizationId()) && !CollectionUtils.isEmpty(o.getRoles()))
      .findFirst();
  }

  private static Optional<UserOrganizationRoles> getUserOrganizationRoles(String organizationIpaCode, UserInfo loggedUser) {
    return loggedUser.getOrganizations().stream()
      .filter(o -> organizationIpaCode.equals(o.getOrganizationIpaCode()) && !CollectionUtils.isEmpty(o.getRoles()))
      .findFirst();
  }

  private static Optional<UserOrganizationRoles> getUserOrganizationRolesFromOrgFiscalCode(String organizationFiscalCode, UserInfo loggedUser) {
    return loggedUser.getOrganizations().stream()
      .filter(o -> organizationFiscalCode.equals(o.getOrganizationFiscalCode()) && !CollectionUtils.isEmpty(o.getRoles()))
      .findFirst();
  }
}
