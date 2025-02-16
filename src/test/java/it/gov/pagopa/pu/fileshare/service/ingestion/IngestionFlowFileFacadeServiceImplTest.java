package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileAlreadyExistsException;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileFacadeServiceImplTest {

  @Mock
  private UserAuthorizationService userAuthorizationServiceMock;
  @Mock
  private FileService fileServiceMock;
  @Mock
  private FileStorerService fileStorerServiceMock;
  @Mock
  private FoldersPathsConfig foldersPathsConfigMock;
  @Mock
  private IngestionFlowFileService ingestionFlowFileServiceMock;
  @Mock
  private IngestionFlowFileDTOMapper ingestionFlowFileDTOMapperMock;
  private IngestionFlowFileFacadeServiceImpl ingestionFlowFileService;
  private static final String VALID_FILE_EXTENSION = ".zip";
  private static final String ARCHIVED_SUB_FOLDER = "Archived";

  @BeforeEach
  void setUp() {
    ingestionFlowFileService = new IngestionFlowFileFacadeServiceImpl(
      userAuthorizationServiceMock,
      fileServiceMock,
      fileStorerServiceMock,
      foldersPathsConfigMock,
      ingestionFlowFileServiceMock,
      ingestionFlowFileDTOMapperMock,
      VALID_FILE_EXTENSION,
      ARCHIVED_SUB_FOLDER);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      userAuthorizationServiceMock,
      fileServiceMock,
      fileStorerServiceMock,
      foldersPathsConfigMock,
      ingestionFlowFileServiceMock,
      ingestionFlowFileDTOMapperMock);
  }

  @Test
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk() {
    String accessToken = "TOKEN";
    long organizationId = 1L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String receiptFilePath = "/receipt";
    String filePath = "/filepath";
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test" + VALID_FILE_EXTENSION,
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    Long expectedIngestionFlowFileId = 1L;
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);
    Mockito.when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    Mockito.when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(filePath);
    Mockito.when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(file,
        IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA, organizationId, filePath))
      .thenReturn(ingestionFlowFileRequestDTO);
    Mockito.when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
      fileName, file, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    Mockito.verify(fileServiceMock).validateFile(file, VALID_FILE_EXTENSION);
  }

  @Test
  void givenAlreadyUploadedWhenThenFileAlreadyExistsException() {
    // Given
    String accessToken = "TOKEN";
    UserInfo userInfo = TestUtils.getSampleUser();
    long organizationId = 1L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String receiptFilePath = "receipt";
    String fileName = "test.txt";

    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "orginalFileName.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Mockito.when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);

    // When
    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic.when(() -> Files.exists(
          organizationBasePath
            .resolve(receiptFilePath)
            .resolve(fileName + ".cipher")))
        .thenReturn(true);

      Assertions.assertThrows(FileAlreadyExistsException.class, () -> ingestionFlowFileService
        .uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
          fileName, file, userInfo, accessToken));

      Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, userInfo, accessToken);
      Mockito.verify(fileServiceMock).validateFile(file, VALID_FILE_EXTENSION);
    }
  }

  @Test
  void givenAlreadyArchivedWhenThenFileAlreadyExistsException() {
    // Given
    String accessToken = "TOKEN";
    UserInfo userInfo = TestUtils.getSampleUser();
    long organizationId = 1L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String receiptFilePath = "receipt";
    String fileName = "test.txt";

    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "originalFileName.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Mockito.when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);

    // When
    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic.when(() -> Files.exists(
          organizationBasePath
            .resolve(receiptFilePath)
            .resolve(fileName + ".cipher")))
        .thenReturn(false);
      filesMockedStatic.when(() -> Files.exists(
          organizationBasePath
            .resolve(receiptFilePath)
            .resolve(ARCHIVED_SUB_FOLDER)
            .resolve(fileName + ".cipher")))
        .thenReturn(true);

      Assertions.assertThrows(FileAlreadyExistsException.class, () -> ingestionFlowFileService
        .uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
          fileName, file, userInfo, accessToken));

      Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, userInfo, accessToken);
      Mockito.verify(fileServiceMock).validateFile(file, VALID_FILE_EXTENSION);
    }
  }

  @Test
  void givenAuthorizedUserWhenDownloadIngestionFlowFileThenReturnFileResource() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName).resolve(ARCHIVED_SUB_FOLDER);

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setStatus(IngestionFlowFile.StatusEnum.COMPLETED);

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
      .thenReturn(organizationBasePath);

    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    Mockito.verify(ingestionFlowFileServiceMock).getIngestionFlowFile(ingestionFlowFileId, accessToken);
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
  }

  @Test
  void givenIngestionFlowFileInProgressWhenDownloadIngestionFlowFileThenReturnFilePath() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName);

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setStatus(IngestionFlowFile.StatusEnum.PROCESSING);

    InputStream decryptedInputStream = Mockito.mock(ByteArrayInputStream.class);

    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId)).thenReturn(organizationBasePath);
    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);
    Mockito.when(fileStorerServiceMock.decryptFile(fullFilePath, fileName)).thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(fileName, result.getFileName());
    Mockito.verify(fileStorerServiceMock).decryptFile(fullFilePath, fileName);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

}
