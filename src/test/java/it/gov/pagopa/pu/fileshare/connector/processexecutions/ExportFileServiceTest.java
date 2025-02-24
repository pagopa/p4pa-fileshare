package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.ExportFileClient;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportFileServiceTest {

  @Mock
  private ExportFileClient clientMock;

  private ExportFileService service;

  @BeforeEach
  void init(){
    service = new ExportFileServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetIngestionFlowFileThenInvokeClient(){
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    UserOrganizationRoles userAdminRole = new UserOrganizationRoles();
    userAdminRole.setRoles(List.of("TEST","ROLE_ADMIN"));
    userAdminRole.setOrganizationId(1L);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userAdminRole));
    ExportFile expectedResult = new ExportFile();

    Mockito.when(clientMock.getExportFile(Mockito.eq(1L), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    ExportFile result = service.getExportFile(1L, organizationId, user, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenGetIngestionFlowFileUnauthorizedUserThenThrowException(){
    // Given
    Long organizationId = 1L;
    String accessToken = "ACCESSTOKEN";
    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("TEST");
    ExportFile expectedResult = new ExportFile();

    Mockito.when(clientMock.getExportFile(Mockito.eq(1L), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    Executable exec = () -> service.getExportFile(1L, organizationId, user, accessToken);

    // Then
    Assertions.assertThrows(UnauthorizedFileDownloadException.class, exec);
  }
}
