package it.gov.pagopa.pu.fileshare.service.export;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.ExportFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserOrganizationRoles;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
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
  void givenAuthorizedUserWhenDownloadExportFileThenReturnFileResource()
    throws FileNotFoundException {
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
    exportFile.setStatus(ExportFile.StatusEnum.COMPLETED);
    exportFile.setOperatorExternalId("TEST");

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);

    Mockito.when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    Mockito.verify(exportFileServiceMock).getExportFile(exportFileId, accessToken);
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
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
    exportFile.setStatus(ExportFile.StatusEnum.COMPLETED);
    exportFile.setOperatorExternalId("TEST");

    Mockito.when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    Executable exec = () -> exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertThrows(UnauthorizedFileDownloadException.class, exec);

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    Mockito.verify(exportFileServiceMock).getExportFile(exportFileId, accessToken);
  }

  @Test
  void givenExportFileInProgressWhenDownloadExportFileThenReturnFilePath()
    throws FileNotFoundException {
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
    exportFile.setStatus(ExportFile.StatusEnum.PROCESSING);
    exportFile.setOperatorExternalId("TEST");

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationBasePath);
    Mockito.when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);
    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenExportFileNotFoundWhenDownloadExportFileThenThrowException() {
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

    Mockito.when(exportFileServiceMock.getExportFile(exportFileId, accessToken)).thenReturn(null);

    Executable exec = () -> exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertThrows(FileNotFoundException.class, exec);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

}
