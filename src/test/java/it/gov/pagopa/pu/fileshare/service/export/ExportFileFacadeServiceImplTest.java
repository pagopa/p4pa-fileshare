package it.gov.pagopa.pu.fileshare.service.export;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.ExportFileClient;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  private ExportFileClient exportFileClientMock;
  @Mock
  private ExportFileFacadeServiceImpl exportFileService;
  private static final String ARCHIVED_SUB_FOLDER = "Archived";

  @BeforeEach
  void setUp() {
    exportFileService = new ExportFileFacadeServiceImpl(
      userAuthorizationServiceMock,
      fileStorerServiceMock,
      exportFileClientMock,
      ARCHIVED_SUB_FOLDER);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      userAuthorizationServiceMock,
      fileStorerServiceMock,
      exportFileClientMock);
  }

  @Test
  void givenAuthorizedUserWhenDownloadExportFileThenReturnFileResource() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long exportFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName).resolve(ARCHIVED_SUB_FOLDER);

    UserInfo user = TestUtils.getSampleUser();

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(organizationId);
    exportFile.setFileName(fileName);
    exportFile.setFilePathName(filePathName);
    exportFile.setStatus(ExportFile.StatusEnum.COMPLETED);

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);

    Mockito.when(exportFileClientMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);

    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    Mockito.verify(exportFileClientMock).getExportFile(exportFileId, accessToken);
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
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

    UserInfo user = TestUtils.getSampleUser();

    ExportFile exportFile = new ExportFile();
    exportFile.setOrganizationId(organizationId);
    exportFile.setFileName(fileName);
    exportFile.setFilePathName(filePathName);
    exportFile.setStatus(ExportFile.StatusEnum.PROCESSING);

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationBasePath);
    Mockito.when(exportFileClientMock.getExportFile(exportFileId, accessToken)).thenReturn(exportFile);
    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = exportFileService.downloadExportFile(organizationId, exportFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

}
