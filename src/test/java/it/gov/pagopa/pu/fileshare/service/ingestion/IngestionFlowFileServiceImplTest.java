package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileDecryptionException;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileServiceImplTest {

  @Mock
  private UserAuthorizationService userAuthorizationServiceMock;
  @Mock
  private FileService fileServiceMock;
  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private FoldersPathsConfig foldersPathsConfigMock;
  @Mock
  private IngestionFlowFileClient ingestionFlowFileClientMock;
  @Mock
  private IngestionFlowFileDTOMapper ingestionFlowFileDTOMapperMock;
  private IngestionFlowFileServiceImpl ingestionFlowFileService;
  private static final String VALID_FILE_EXTENSION = ".zip";
  private static final String ARCHIVED_SUB_FOLDER = "Archived";

  @BeforeEach
  void setUp() {
    ingestionFlowFileService = new IngestionFlowFileServiceImpl(
      userAuthorizationServiceMock,
      fileServiceMock,
      fileStorerServiceMock,
      foldersPathsConfigMock,
      ingestionFlowFileClientMock,
      ingestionFlowFileDTOMapperMock,
      VALID_FILE_EXTENSION,
      ARCHIVED_SUB_FOLDER);
  }

  @Test
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk() {
    String accessToken = "TOKEN";
    long organizationId = 1L;
    String receiptFilePath = "/receipt";
    String filePath = "/filepath";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test" + VALID_FILE_EXTENSION,
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    String expectedIngestionFlowFileId = "INGESTIONFLOWFILEID";
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    Mockito.when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath))
      .thenReturn(filePath);
    Mockito.when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(file,
        IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA, organizationId, filePath))
      .thenReturn(ingestionFlowFileRequestDTO);
    Mockito.when(ingestionFlowFileClientMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    String result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
      file, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    Mockito.verify(fileServiceMock).validateFile(file, VALID_FILE_EXTENSION);
    Mockito.verifyNoMoreInteractions(userAuthorizationServiceMock, fileServiceMock,
      foldersPathsConfigMock, fileStorerServiceMock, ingestionFlowFileDTOMapperMock, ingestionFlowFileClientMock);
  }

  @Test
  void givenAuthorizedUserWhenDownloadIngestionFlowFileThenReturnFileResource() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String sharedFolderPath = "/shared";
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path organizationPath = Paths.get(sharedFolderPath, String.valueOf(organizationId));
    String fullFilePath = organizationPath + "/" + filePathName + "/" + ARCHIVED_SUB_FOLDER + "/" + fileName;

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setStatus(IngestionFlowFile.StatusEnum.COMPLETED);

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationPath);

    Mockito.when(ingestionFlowFileClientMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    Mockito.verify(ingestionFlowFileClientMock).getIngestionFlowFile(ingestionFlowFileId, accessToken);
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
  }

  @Test
  void givenNullDecryptedInputStreamWhenDownloadIngestionFlowFileThenThrowFileDecryptionException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String sharedFolderPath = "/shared";
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path organizationPath = Paths.get(sharedFolderPath, String.valueOf(organizationId));
    String fullFilePath = organizationPath + "/" + filePathName + "/" + ARCHIVED_SUB_FOLDER + "/" + fileName;

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setStatus(IngestionFlowFile.StatusEnum.COMPLETED);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationPath);
    Mockito.when(ingestionFlowFileClientMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);
    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(null);

    FileDecryptionException exception = Assertions.assertThrows(FileDecryptionException.class,
      () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Assertions.assertEquals("File could not be decrypted or was not found", exception.getMessage());
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
  }

  @Test
  void givenNullOrganizationBasePathWhenDownloadIngestionFlowFileThenThrowIllegalStateException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(null);

    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
      () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Assertions.assertEquals("Organization base path cannot be null.", exception.getMessage());
    Mockito.verify(fileStorerServiceMock).buildOrganizationBasePath(organizationId);
  }

  @Test
  void givenIngestionFlowFileInProgressWhenDownloadIngestionFlowFileThenReturnFilePath() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String sharedFolderPath = "/shared";
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path organizationPath = Paths.get(sharedFolderPath, String.valueOf(organizationId));
    String fullFilePath = organizationPath + "/" + filePathName + "/" + fileName;

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setStatus(IngestionFlowFile.StatusEnum.PROCESSING);

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationPath);
    Mockito.when(ingestionFlowFileClientMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);
    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
  }

}
