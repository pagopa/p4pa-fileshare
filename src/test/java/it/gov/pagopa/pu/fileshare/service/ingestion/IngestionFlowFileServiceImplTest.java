package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.connector.organization.client.OrganizationClient;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileServiceImplTest {

  @Mock
  private OrganizationClient organizationClientMock;
  private IngestionFlowFileServiceImpl ingestionFlowFileService;
  private final String accessToken = "TOKEN";
  private final long organizationId = 1L;

  @BeforeEach
  void setUp() {
    ingestionFlowFileService = new IngestionFlowFileServiceImpl(organizationClientMock);
  }

  @Test
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk(){
    Organization org = new Organization();
    org.setIpaCode("ORG2");
    Organization spyOrg = Mockito.spy(org);
    Mockito.when(organizationClientMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT,
      null, TestUtils.getSampleUser(),accessToken);

    Mockito.verify(spyOrg, Mockito.times(2)).getIpaCode();
  }

  @Test
  void givenNoRolesWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    Organization spyOrg = Mockito.spy(org);
    Mockito.when(organizationClientMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    UserInfo user = TestUtils.getSampleUser();
    user.getOrganizations().forEach(o->o.setRoles(Collections.emptyList()));

    try {
      ingestionFlowFileService.uploadIngestionFlowFile(organizationId,
        IngestionFlowFileType.RECEIPT,
        null, user, accessToken);
      Assertions.fail("Expected AuthorizationDeniedException");
    }catch(AuthorizationDeniedException e){
      Mockito.verify(spyOrg, Mockito.times(2)).getIpaCode();
    }
  }

  @Test
  void givenNoMatchingIpaCodeWhenUploadIngestionFlowFileThenAuthorizationDeniedException(){
    Organization org = new Organization();
    org.setIpaCode("ipaCode");
    Organization spyOrg = Mockito.spy(org);
    Mockito.when(organizationClientMock.getOrganizationById(organizationId, accessToken))
      .thenReturn(spyOrg);

    try {
      ingestionFlowFileService.uploadIngestionFlowFile(organizationId,
        IngestionFlowFileType.RECEIPT,
        null, TestUtils.getSampleUser(), accessToken);
      Assertions.fail("Expected AuthorizationDeniedException");
    }catch(AuthorizationDeniedException e){
      Mockito.verify(spyOrg, Mockito.times(2)).getIpaCode();
    }
  }
}
