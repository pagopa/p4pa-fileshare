package it.gov.pagopa.pu.fileshare.service.export;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.ExportFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.auth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFileStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportFileFacadeServiceImplTest {

  @Mock
  private UserAuthorizationService userAuthorizationServiceMock;
  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private ExportFileService exportFileServiceMock;
  @Mock
  private ExportFileFacadeServiceImpl exportFileService;

  @BeforeEach
  void setUp() {
    exportFileService = new ExportFileFacadeServiceImpl(
      userAuthorizationServiceMock,
      fileStorerServiceMock,
      exportFileServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      userAuthorizationServiceMock,
      fileStorerServiceMock,
      exportFileServiceMock);
  }

  @Test
  void givenAuthorizedUserWhenDownloadExportFileThenReturnFileResource() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName);

    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST", "ADMIN"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("TEST");

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(organizationId);
    exportFile.setFileName(fileName);
    exportFile.setFilePathName(filePathName);
    exportFile.setStatus(ExportFileStatus.COMPLETED);
    exportFile.setOperatorExternalId("TEST");

    InputStream decryptedInputStream = mock(ByteArrayInputStream.class);

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);

    when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    verify(exportFileServiceMock).getExportFile(exportFileId, accessToken);
    verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
  }

  @Test
  void givenUnauthorizedOrganizationWhenDownloadExportFileThenThrowException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;
    String filePathName = "examplePath";
    String fileName = "testFile.zip";

    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("UNAUTHORIZED_OPERATOR");

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(-1L);
    exportFile.setFileName(fileName);
    exportFile.setFilePathName(filePathName);
    exportFile.setStatus(ExportFileStatus.COMPLETED);
    exportFile.setOperatorExternalId("TEST");

    when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    Executable exec = () -> exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertThrows(AuthorizationDeniedException.class, exec);

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    verify(exportFileServiceMock).getExportFile(exportFileId, accessToken);
  }

  @Test
  void givenUnauthorizedUserWhenDownloadExportFileThenThrowException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;
    String filePathName = "examplePath";
    String fileName = "testFile.zip";

    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("UNAUTHORIZED_OPERATOR");

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(organizationId);
    exportFile.setFileName(fileName);
    exportFile.setFilePathName(filePathName);
    exportFile.setStatus(ExportFileStatus.COMPLETED);
    exportFile.setOperatorExternalId("TEST");

    when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    Executable exec = () -> exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertThrows(UnauthorizedFileDownloadException.class, exec);

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    verify(exportFileServiceMock).getExportFile(exportFileId, accessToken);
  }

  @Test
  void givenExportFileInProgressWhenDownloadExportFileThenReturnFilePath() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName);

    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST", "ADMIN"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("TEST");

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(organizationId);
    exportFile.setFileName(fileName);
    exportFile.setFilePathName(filePathName);
    exportFile.setStatus(ExportFileStatus.PROCESSING);
    exportFile.setOperatorExternalId("TEST");

    InputStream decryptedInputStream = mock(ByteArrayInputStream.class);

    when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationBasePath);
    when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);
    when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());
    verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenExportFileNotFoundWhenDownloadExportFileThenThrowException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;

    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST", "ADMIN"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("TEST");

    when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(null);

    Executable exec = () -> exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertThrows(FileNotFoundException.class, exec);
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenExportFileNotReadyWhenDownloadExportFileThenThrowException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;

    UserOrganizationRoles userTestRole = new UserOrganizationRoles();
    userTestRole.setRoles(List.of("TEST", "ADMIN"));
    userTestRole.setOrganizationId(organizationId);
    UserInfo user = new UserInfo();
    user.setOrganizations(List.of(userTestRole));
    user.setMappedExternalUserId("TEST");

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(organizationId);
    exportFile.setStatus(ExportFileStatus.REQUESTED);
    exportFile.setOperatorExternalId("TEST");

    when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    Executable exec = () -> exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertThrows(FileNotFoundException.class, exec);
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

}
