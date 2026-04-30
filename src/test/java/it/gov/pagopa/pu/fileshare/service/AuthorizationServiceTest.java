package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.auth.client.AuthnClient;
import it.gov.pagopa.pu.fileshare.connector.organization.OrganizationService;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

  @Mock
  private AuthnClient authClientImplMock;
  @Mock
  private OrganizationService organizationServiceMock;

  @InjectMocks
  private AuthorizationService authorizationService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      authClientImplMock,
      organizationServiceMock
    );
  }

  @Test
  void givenValidAccessTokenWhenValidateTokenThenOk() {
    UserInfo ui = new UserInfo();
    when(authClientImplMock.getUserInfo("ACCESSTOKEN")).thenReturn(ui);
    UserInfo result = authorizationService.validateToken("ACCESSTOKEN");

    Assertions.assertEquals(ui, result);
  }

  @Test
  void givenInvalidAccessTokenWhenValidateTokenThenInvalidAccessTokenException() {
    when(authClientImplMock.getUserInfo("INVALIDACCESSTOKEN")).thenThrow(new InvalidAccessTokenException("INVALID_ACCESS_TOKEN", "Bad Access Token provided"));
    InvalidAccessTokenException result = Assertions.assertThrows(InvalidAccessTokenException.class,
      () -> authorizationService.validateToken("INVALIDACCESSTOKEN"));

    Assertions.assertEquals("[INVALID_ACCESS_TOKEN] Bad Access Token provided", result.getMessage());
  }

  @Test
  void givenAdminRoleWhenValidateAdminRoleOrBrokerAdminThenReturnOrgFiscalCode() {
    // Given
    String accessToken = "accessToken";
    long organizationId = 1L;
    String expectedOrgFiscalCode = "ORG_FISCAL_CODE";

    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(organizationId);
    userAdminRole.setOrganizationFiscalCode(expectedOrgFiscalCode);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));

    // When
    String result = authorizationService.validateAdminRoleOrBrokerAdmin(organizationId, userInfo, accessToken);

    // Then
    Assertions.assertSame(expectedOrgFiscalCode, result);
  }

  @Test
  void givenNoAdminRoleAndNotOrganizationWhenValidateAdminRoleThenAuthorizationDeniedException() {
    // Given
    Long organizationId = 2L;
    String accessToken = "accessToken";

    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(null);

    // When
    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> authorizationService.validateAdminRoleOrBrokerAdmin(organizationId, userInfo, accessToken));

    // Then
    Assertions.assertEquals("[USER_UNAUTHORIZED] Access denied on organizationId " + 2L + " to user externalUserId", result.getMessage());
  }

  @Test
  void givenNoAdminRoleAndNotOrgBrokerIdWhenValidateAdminRoleThenAuthorizationDeniedException() {
    // Given
    Long organizationId = 2L;
    String accessToken = "accessToken";

    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");

    Organization organization = new Organization();

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(organization);

    // When
    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> authorizationService.validateAdminRoleOrBrokerAdmin(organizationId, userInfo, accessToken));

    // Then
    Assertions.assertEquals("[USER_UNAUTHORIZED] Access denied on organizationId " + 2L + " to user externalUserId", result.getMessage());
  }

  @Test
  void givenNoAdminRoleAndBrokerIdNotMatchedWhenValidateAdminRoleThenAuthorizationDeniedException() {
    // Given
    Long organizationId = 2L;
    Long brokerId = 1L;
    String accessToken = "accessToken";

    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo userInfo = new UserInfo();
    userInfo.setBrokerId(-1L);
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");

    Organization org = new Organization();
    org.setBrokerId(brokerId);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(org);

    // When
    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> authorizationService.validateAdminRoleOrBrokerAdmin(organizationId, userInfo, accessToken));

    // Then
    Assertions.assertEquals("[USER_UNAUTHORIZED] Access denied on organizationId " + 2L + " to user externalUserId", result.getMessage());
  }

  @Test
  void givenNoAdminRoleAndBrokerIdMatchedAndNotAdminOnBrokerWhenValidateAdminRoleThenAuthorizationDeniedException() {
    // Given
    Long organizationId = 2L;
    Long brokerId = 10L;
    String accessToken = "accessToken";
    String brokerFiscalCode = "BROKER_FISCAL_CODE";

    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo userInfo = new UserInfo();
    userInfo.setBrokerId(brokerId);
    userInfo.setBrokerFiscalCode(brokerFiscalCode);
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");

    Organization org = new Organization();
    org.setBrokerId(brokerId);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(org);

    // When
    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> authorizationService.validateAdminRoleOrBrokerAdmin(organizationId, userInfo, accessToken));

    // Then
    Assertions.assertEquals("[USER_UNAUTHORIZED] Access denied on brokerOrgFiscalCode BROKER_FISCAL_CODE to user externalUserId", result.getMessage());
  }

  @Test
  void givenNoAdminRoleAndBrokerIdMatchedAndAdminOnBrokerWhenValidateAdminRoleThenReturnOrgFiscalCode() {
    // Given
    Long organizationId = 2L;
    Long brokerId = 1L;
    String accessToken = "accessToken";
    String brokerFiscalCode = "BROKER_FISCAL_CODE";
    String expectedOrgFiscalCode = "ORG_FISCAL_CODE";

    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationFiscalCode(brokerFiscalCode);
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo userInfo = new UserInfo();
    userInfo.setBrokerId(brokerId);
    userInfo.setBrokerFiscalCode(brokerFiscalCode);
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");

    Organization org = new Organization();
    org.setBrokerId(brokerId);
    org.setOrgFiscalCode(expectedOrgFiscalCode);

    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(org);

    // When
    String result = authorizationService.validateAdminRoleOrBrokerAdmin(organizationId, userInfo, accessToken);

    // Then
    Assertions.assertSame(expectedOrgFiscalCode, result);
  }

  @Test
  void givenAdminRoleWhenValidateAdminRoleThenOK() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));

    AuthorizationService.validateAdminRole(1L,userInfo);
  }

  @Test
  void givenNoAdminRoleWhenValidateAdminRoleThenAuthorizationDeniedException() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");

    AuthorizationDeniedException result = Assertions.assertThrows(
      AuthorizationDeniedException.class,
      () -> AuthorizationService.validateAdminRole(2L,userInfo));

    Assertions.assertEquals("[USER_UNAUTHORIZED] Access denied on organizationId " + 2L + " to user externalUserId", result.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
    "orgFiscalCode, orgFiscalCode, false",
    "orgFiscalCode, adminOrgFiscalCode, true"
  })
  void testValidateBrokerAdminRole(String brokerFiscalCode,
                                   String adminOrgFiscalCode,
                                   boolean expectError) {
    // Given
    UserInfo userInfo = new UserInfo();
    userInfo.setMappedExternalUserId("userId");
    userInfo.setBrokerFiscalCode(brokerFiscalCode);
    userInfo.setOrganizations(List.of(
      new UserOrganizationRoles("OID1", 1L, "IPA_1", adminOrgFiscalCode, "email", List.of("TEST", "ROLE_ADMIN")),
      new UserOrganizationRoles("OID2", 2L, "IPA_2", brokerFiscalCode, "email", List.of("TEST"))
    ));

    // When/Then
    if (expectError) {
      Assertions.assertThrows(AuthorizationDeniedException.class,
        () -> AuthorizationService.validateBrokerAdminRole(userInfo)
      );
    } else {
      Assertions.assertDoesNotThrow(() -> AuthorizationService.validateBrokerAdminRole(userInfo));
    }
  }

  @Test
  void givenAdminRoleWhenIsAdminRoleThenOK() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    boolean adminRole = AuthorizationService.isAdminRole(1L, userInfo);

    Assertions.assertTrue(adminRole);
  }

  @Test
  void givenNoAdminRoleWhenIsAdminRoleThenAuthorizationDeniedException() {
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(2L);
    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userAdminRole,userTestRole));
    userInfo.setMappedExternalUserId("externalUserId");
    boolean adminRole = AuthorizationService.isAdminRole(2L, userInfo);

    Assertions.assertFalse(adminRole);
  }

  @Test
  void givenUserEnabledToOrganizationIdWhenValidateUserForOrganizationIdThenOk() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(List.of("TEST"));
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertDoesNotThrow(() -> AuthorizationService.validateUserForOrganizationId(1L, userInfo));
  }

  @Test
  void givenUserNotEnabledToOrganizationIdWhenValidateUserForOrganizationIdThenUnauthorized() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(List.of("TEST"));
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateUserForOrganizationId(2L, userInfo));
  }

  @Test
  void givenUserWithEmptyRolesWhenValidateUserForOrganizationIdThenUnauthorized() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(List.of());
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateUserForOrganizationId(1L, userInfo));
  }

  @Test
  void givenUserWithNullRolesWhenValidateUserForOrganizationIdThenUnauthorized() {
    UserOrganizationRoles userOrgRole = new UserOrganizationRoles();
    userOrgRole.setRoles(null);
    userOrgRole.setOrganizationId(1L);

    UserInfo userInfo = new UserInfo();
    userInfo.setOrganizations(List.of(userOrgRole));

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> AuthorizationService.validateUserForOrganizationId(1L, userInfo));
  }

  @ParameterizedTest
  @CsvSource({
    "true, IPA_2, true",  // Valid admin user for the organization
    "true, IPA_1, false", // User without admin role for the organization
    "true, IPA_3, false", // Organization not associated with the user
    "false, IPA_2, false"  // Invalid user (no logged-in user)
  })
  void testIsAdminRole(boolean logged, String organizationIpaCode, boolean expectedResult) {
    // Given
    UserInfo expectedUserInfo = null;
    if (logged) {
      expectedUserInfo = new UserInfo();
      expectedUserInfo.setMappedExternalUserId("USERID");
      expectedUserInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of(AuthorizationService.ROLE_ADMIN))
      ));
    }

    // When
    boolean result = AuthorizationService.isAdminRole(organizationIpaCode, expectedUserInfo);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }


  @ParameterizedTest
  @CsvSource(value={
    "USERID, IPA_1, CF_1",  // Valid organization with fiscal code
    "USERID, IPA_2, CF_2",  // Another valid organization with fiscal code
    "USERID, IPA_3, null",  // Organization not associated with the user
    "null, IPA_1, null",    // Null user
    "USERID, null, null"    // Null organization IPA code
  }, nullValues={"null"})
  void testGetOrgFiscalCodeFromUserInfo(String userId, String organizationIpaCode, String expectedFiscalCode) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    String result = AuthorizationService.getOrgFiscalCodeFromUserInfo(userInfo, organizationIpaCode);

    // Then
    Assertions.assertEquals(expectedFiscalCode, result);
  }

  @ParameterizedTest
  @CsvSource(value={
    "USERID, IPA_1, 1",
    "USERID, IPA_2, 2",
    "USERID, IPA_3, null",
    "null, IPA_1, null",
    "USERID, null, null"
  }, nullValues={"null"})
  void testGetOrganizationIdFromUserInfo(String userId, String organizationIpaCode, Long expectedId) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    Long result = AuthorizationService.getOrganizationIdFromUserInfo(userInfo, organizationIpaCode);

    // Then
    Assertions.assertEquals(expectedId, result);
  }

  @ParameterizedTest
  @CsvSource(value={
    "USERID, 1, IPA_1",  // Valid organization
    "USERID, 2, IPA_2",  // Another valid organization
    "USERID, 3, null",   // Organization not associated with the user
    "null, 1, null",     // Null user
    "USERID, null, null" // Null organizationId
  }, nullValues={"null"})
  void testGetOrgIpaCodeFromUserInfo(String userId, Long organizationId, String expectedIpaCode) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    String result = AuthorizationService.getOrgIpaCodeFromUserInfo(userInfo, organizationId);

    // Then
    Assertions.assertEquals(expectedIpaCode, result);
  }

  @ParameterizedTest
  @CsvSource(value={
    "USERID, CF_1, 1",
    "USERID, CF_2, 2",
    "USERID, CF_3, null",
    "null, CF_1, null",
    "USERID, null, null"
  }, nullValues={"null"})
  void testGetOrganizationIdFromOrgFiscalCode(String userId, String organizationFiscalCode, Long expectedId) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    Long result = AuthorizationService.getOrganizationIdFromOrgFiscalCode(userInfo, organizationFiscalCode);

    // Then
    Assertions.assertEquals(expectedId, result);
  }

  @ParameterizedTest
  @CsvSource(value={
    "USERID, 1, CF_1",  // Valid organization
    "USERID, 2, CF_2",  // Another valid organization
    "USERID, 3, null",   // Organization not associated with the user
    "null, 1, null",     // Null user
    "USERID, null, null" // Null organizationId
  }, nullValues={"null"})
  void testOrgFiscalCodeFromUserInfo(String userId, Long organizationId, String organizationFiscalCode) {
    // Given
    UserInfo userInfo = null;
    if (userId != null) {
      userInfo = new UserInfo();
      userInfo.setMappedExternalUserId(userId);
      userInfo.setOrganizations(List.of(
        new UserOrganizationRoles("OID1", 1L, "IPA_1", "CF_1", "email", List.of("ROLE_USER")),
        new UserOrganizationRoles("OID2", 2L, "IPA_2", "CF_2", "email", List.of("ROLE_ADMIN"))
      ));
    }

    // When
    String result = AuthorizationService.getOrgFiscalCodeFromUserInfo(userInfo, organizationId);

    // Then
    Assertions.assertEquals(organizationFiscalCode, result);
  }
}
