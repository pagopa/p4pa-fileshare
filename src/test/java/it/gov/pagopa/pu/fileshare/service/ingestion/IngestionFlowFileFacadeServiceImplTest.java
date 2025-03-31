package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileAlreadyExistsException;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.mockito.Mockito.when;

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
      ARCHIVED_SUB_FOLDER);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      userAuthorizationServiceMock,
      fileServiceMock,
      foldersPathsConfigMock,
      ingestionFlowFileServiceMock,
      ingestionFlowFileDTOMapperMock);
  }

  @Test
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk() {
    String accessToken = "TOKEN";
    long organizationId = 1L;
    String receiptFilePath = "/receipt";
    String filePath = "/filepath";
    String fileName = "fileName.txt";
    SaveFileResultDTO saveFileResult = new SaveFileResultDTO(filePath, "this is a test file".getBytes());
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    Long expectedIngestionFlowFileId = 1L;
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    Mockito.when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    Mockito.when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(saveFileResult);
    Mockito.when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(file,
        IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA, organizationId, filePath))
      .thenReturn(ingestionFlowFileRequestDTO);
    Mockito.when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
      fileName, file, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    Mockito.verify(fileServiceMock).validateFile(file);
  }

  @Test
  void givenAlreadyUploadedWhenThenFileAlreadyExistsException() {
    // Given
    String accessToken = "TOKEN";
    UserInfo userInfo = TestUtils.getSampleUser();
    long organizationId = 1L;

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
    when(fileStorerServiceMock.checkIfAlreadyUploadedOrArchived(
      organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName))
      .thenReturn(true);

    // When
    Assertions.assertThrows(FileAlreadyExistsException.class, () -> ingestionFlowFileService
      .uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
        fileName, file, userInfo, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, userInfo, accessToken);
    Mockito.verify(fileServiceMock).validateFile(file);
  }

  @Test
  void givenAuthorizedUserWhenDownloadIngestionFlowFileThenReturnFileResource() {
    givenAuthorizedUserWhenDownloadIngestionFlowFileThenReturnFileResource(false);
  }
  @Test
  void givenAuthorizedAdminUserWhenDownloadIngestionFlowFileThenReturnFileResource() {
    givenAuthorizedUserWhenDownloadIngestionFlowFileThenReturnFileResource(true);
  }
  void givenAuthorizedUserWhenDownloadIngestionFlowFileThenReturnFileResource(boolean isAdmin) {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName).resolve(ARCHIVED_SUB_FOLDER);

    UserInfo user = isAdmin? TestUtils.getSampleAdminUser() : TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    if(!isAdmin){
      ingestionFlowFile.setOperatorExternalId(user.getMappedExternalUserId());
    }
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

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
  void givenUnauthorizedOrganizationWhenDownloadIngestionFlowFileThenThrowAuthorizationDeniedException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(-1L);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenUnauthorizedUserWhenDownloadIngestionFlowFileThenThrowUnauthorizedFileDownloadException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(1L);
    ingestionFlowFile.setOperatorExternalId("OTHERUSER");
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(UnauthorizedFileDownloadException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowFileNotFoundWhenDownloadIngestionFlowFileThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    Mockito.when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(null);

    Assertions.assertThrows(FileNotFoundException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);

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
