package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.organization.OrganizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import it.gov.pagopa.pu.p4paorganization.dto.generated.OrganizationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.Collections;

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
    Organization spyOrg = Mockito.spy(org);
    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    userAuthorizationService.checkUserAuthorization(organizationId,TestUtils.getSampleUser(),accessToken);

    Mockito.verify(spyOrg, Mockito.times(2)).getIpaCode();
  }

  @Test
  void givenNoRolesWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    org.setStatus(OrganizationStatus.ACTIVE);
    Organization spyOrg = Mockito.spy(org);
    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    UserInfo user = TestUtils.getSampleUser();
    user.getOrganizations().forEach(o->o.setRoles(Collections.emptyList()));

    try {
      userAuthorizationService.checkUserAuthorization(organizationId,TestUtils.getSampleUser(),accessToken);
      Assertions.fail("Expected AuthorizationDeniedException");
    }catch(AuthorizationDeniedException e){
      Mockito.verify(spyOrg, Mockito.times(2)).getIpaCode();
    }
  }

  @Test
  void givenNoMatchingIpaCodeWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    org.setStatus(OrganizationStatus.ACTIVE);
    Organization spyOrg = Mockito.spy(org);
    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    try {
      userAuthorizationService.checkUserAuthorization(organizationId,TestUtils.getSampleUser(),accessToken);
      Assertions.fail("Expected AuthorizationDeniedException");
    }catch(AuthorizationDeniedException e){
      Mockito.verify(spyOrg, Mockito.times(2)).getIpaCode();
    }
  }

  @Test
  void givenNotActiveOrgWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    org.setStatus(OrganizationStatus.DRAFT);
    Mockito.when(organizationServiceMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(org);

    UserInfo user = TestUtils.getSampleUser();

    AuthorizationDeniedException result = Assertions.assertThrows(AuthorizationDeniedException.class, () -> userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken));

    Assertions.assertEquals("[ORGANIZATION_INVALID_STATUS] Access Denied", result.getMessage());
  }
}
