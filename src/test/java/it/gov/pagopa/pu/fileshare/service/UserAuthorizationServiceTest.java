package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.fileshare.connector.organization.OrganizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationServiceTest {

  @Mock
  private OrganizationService organizationServiceMock;
  private UserAuthorizationService userAuthorizationService;
  private final String accessToken = "TOKEN";
  private final long organizationId = 1L;

  @BeforeEach
  void setUp() {
    userAuthorizationService = new UserAuthorizationService(organizationServiceMock);
  }

  @Test
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk(){
    Organization org = new Organization();
    org.setIpaCode("ORG2");
    org.setStatus(OrganizationStatus.ACTIVE);
    Organization spyOrg = spy(org);
    when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    userAuthorizationService.checkUserAuthorization(organizationId,TestUtils.getSampleUser(),accessToken);

    verify(spyOrg, times(2)).getIpaCode();
  }

  @Test
  void givenNoRolesWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    org.setStatus(OrganizationStatus.ACTIVE);
    Organization spyOrg = spy(org);
    when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    UserInfo user = TestUtils.getSampleUser();
    user.getOrganizations().forEach(o->o.setRoles(Collections.emptyList()));

    assertThrows(AuthorizationDeniedException.class, () ->
      userAuthorizationService.checkUserAuthorization(organizationId,user,accessToken),
      "Expected AuthorizationDeniedException");
  }

  @Test
  void givenNoMatchingIpaCodeWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    org.setStatus(OrganizationStatus.ACTIVE);
    UserInfo sampleUser = TestUtils.getSampleUser();
    Organization spyOrg = spy(org);

    when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    assertThrows(AuthorizationDeniedException.class, () ->
      userAuthorizationService.checkUserAuthorization(organizationId, sampleUser,accessToken),
      "Expected AuthorizationDeniedException");
  }

  @Test
  void givenNotActiveOrgWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    org.setStatus(OrganizationStatus.DRAFT);
    when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(org);

    UserInfo user = TestUtils.getSampleUser();

    AuthorizationDeniedException result = assertThrows(AuthorizationDeniedException.class, () -> userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken));

    Assertions.assertEquals("[ORGANIZATION_INVALID_STATUS] Access Denied", result.getMessage());
  }
}
