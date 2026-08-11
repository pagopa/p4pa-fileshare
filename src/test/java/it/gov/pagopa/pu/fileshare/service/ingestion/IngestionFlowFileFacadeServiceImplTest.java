package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.*;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionFlowFileFacadeServiceImplTest {

  private static final String ARCHIVED_SUB_FOLDER = "Archived";
  private static final String ERRORS_SUB_FOLDER = "Error";

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
  @Mock
  private DuplicateIngestionFlowFileRequestHandlerService duplicateIngestionFlowFileRequestHandlerServiceMock;

  private IngestionFlowFileFacadeServiceImpl ingestionFlowFileService;

  @BeforeEach
  void setUp() {
    ingestionFlowFileService = new IngestionFlowFileFacadeServiceImpl(
      ARCHIVED_SUB_FOLDER,
      ERRORS_SUB_FOLDER,
      userAuthorizationServiceMock,
      fileServiceMock,
      fileStorerServiceMock,
      foldersPathsConfigMock,
      ingestionFlowFileServiceMock,
      ingestionFlowFileDTOMapperMock,
      duplicateIngestionFlowFileRequestHandlerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      userAuthorizationServiceMock,
      fileServiceMock,
      fileStorerServiceMock,
      foldersPathsConfigMock,
      ingestionFlowFileServiceMock,
      ingestionFlowFileDTOMapperMock,
      duplicateIngestionFlowFileRequestHandlerServiceMock);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk(boolean alreadyUploaded) {
    String accessToken = "TOKEN";
    long organizationId = 1L;
    String receiptFilePath = "/receipt";
    String filePath = "/filepath";
    String fileName = "fileName1_1.txt";
    SaveFileResultDTO saveFileResult = new SaveFileResultDTO(filePath, "this is a test file".getBytes());
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    Long expectedIngestionFlowFileId = 1L;
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    String fileVersion = "1.1";
    List<String> versionList = List.of("1.0", "1.1", "1.2", "1.3");

    when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    when(fileStorerServiceMock.checkIfAlreadyUploadedOrArchived(organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName))
      .thenReturn(alreadyUploaded);
    when(ingestionFlowFileServiceMock.getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT, accessToken))
      .thenReturn(versionList);
    when(fileServiceMock.validateVersionFromIngestionFlowFilename(versionList, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT))
      .thenReturn(fileVersion);
    when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(saveFileResult);
    when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(null, file,
      IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA, organizationId, filePath, fileName, fileVersion))
      .thenReturn(ingestionFlowFileRequestDTO);
    when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
      fileName, file, null, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    verify(fileServiceMock).validateFile(file);
    if (alreadyUploaded) {
      verify(duplicateIngestionFlowFileRequestHandlerServiceMock)
        .handleDuplicateFile(organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName, accessToken);
    }
  }

  @Test
  void givenFileTypeDPINSTALLMENTSWhenUploadIngestionFlowFileThenOk() {

    String accessToken = "TOKEN";
    long organizationId = 1L;
    String receiptFilePath = "/dp-installment";
    String filePath = "/filepath";
    String fileName = "fileName1_1.txt";
    String fileVersion = "1.1";
    SaveFileResultDTO saveFileResult = new SaveFileResultDTO(filePath, "this is a test file".getBytes());
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    Long expectedIngestionFlowFileId = 1L;
    List<String> versionList = List.of("1.0", "1.1", "1.3", "1.4", "2.0");
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.DP_INSTALLMENTS))
      .thenReturn(receiptFilePath);
    when(fileStorerServiceMock.checkIfAlreadyUploadedOrArchived(organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName))
      .thenReturn(false);
    when(ingestionFlowFileServiceMock.getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, accessToken))
      .thenReturn(versionList);
    when(fileServiceMock.validateVersionFromIngestionFlowFilename(versionList, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS))
      .thenReturn(fileVersion);
    when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(saveFileResult);
    when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(null, file,
      IngestionFlowFileType.DP_INSTALLMENTS, FileOrigin.PAGOPA, organizationId, filePath, fileName, fileVersion))
      .thenReturn(ingestionFlowFileRequestDTO);
    when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.DP_INSTALLMENTS, FileOrigin.PAGOPA,
      fileName, file, null, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    verify(fileServiceMock).validateFile(file);

  }

  @Test
  void givenFileTypeRECEIPTPAGOPAWhenUploadIngestionFlowFileThenOk() {

    String accessToken = "TOKEN";
    long organizationId = 1L;
    String receiptFilePath = "/receipt-pagopa";
    String filePath = "/filepath";
    String fileName = "fileName1_1.txt";
    SaveFileResultDTO saveFileResult = new SaveFileResultDTO(filePath, "this is a test file".getBytes());
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    Long expectedIngestionFlowFileId = 1L;
    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();

    when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT_PAGOPA))
      .thenReturn(receiptFilePath);
    when(fileStorerServiceMock.checkIfAlreadyUploadedOrArchived(organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName))
      .thenReturn(false);
    when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(saveFileResult);
    when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(null, file,
      IngestionFlowFileType.RECEIPT_PAGOPA, FileOrigin.PAGOPA, organizationId, filePath, fileName, null))
      .thenReturn(ingestionFlowFileRequestDTO);
    when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT_PAGOPA, FileOrigin.PAGOPA,
      fileName, file, null, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    verify(fileServiceMock).validateFile(file);

  }

  @Test
  void givenFileTypeDPINSTALLMENTSWithFileNameWithoutValidVersionWhenUploadIngestionFlowFileThenThrowInvalidFileException() {
    String accessToken = "TOKEN";
    long organizationId = 1L;
    String receiptFilePath = "/dp-installment";
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    List<String> versionList = List.of("1.0", "1.1", "1.3", "1.4", "2.0");

    when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.DP_INSTALLMENTS))
      .thenReturn(receiptFilePath);
    when(fileStorerServiceMock.checkIfAlreadyUploadedOrArchived(organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName))
      .thenReturn(false);
    when(ingestionFlowFileServiceMock.getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, accessToken))
      .thenReturn(List.of("1.0", "1.1", "1.3", "1.4", "2.0"));
    doThrow(new InvalidFileException("INVALID_FILE_NAME", "Invalid file version"))
      .when(fileServiceMock).validateVersionFromIngestionFlowFilename(versionList, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS);

    try {
      ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.DP_INSTALLMENTS, FileOrigin.PAGOPA, fileName, file, null, TestUtils.getSampleUser(), accessToken);
    } catch (InvalidFileException e) {
      assertEquals("INVALID_FILE_NAME", e.getCode());
      assertEquals("Invalid file version", e.getMessage());
    }
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    verify(fileServiceMock).validateFile(file);
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

    UserInfo user = isAdmin ? TestUtils.getSampleAdminUser() : TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    if (!isAdmin) {
      ingestionFlowFile.setOperatorExternalId(user.getMappedExternalUserId());
    }
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    InputStream decryptedInputStream = mock(ByteArrayInputStream.class);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);
    when(fileStorerServiceMock.getUploadedOrArchivedPath(organizationId, ARCHIVED_SUB_FOLDER, filePathName, fileName))
      .thenReturn(fullFilePath.resolve(fileName));
    when(fileStorerServiceMock.decryptFile(fullFilePath, fileName))
      .thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken);

    assertNotNull(result);
    assertEquals(fileName, result.getFileName());

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenAuthorizedUserWhenDownloadIngestionFlowErrorsFileThenReturnFileResource() {
    givenAuthorizedUserWhenDownloadIngestionFlowErrorsFileThenReturnFileResource(false);
  }

  @Test
  void givenAuthorizedAdminUserWhenDownloadIngestionFlowErrorsFileThenReturnFileResource() {
    givenAuthorizedUserWhenDownloadIngestionFlowErrorsFileThenReturnFileResource(true);
  }

  void givenAuthorizedUserWhenDownloadIngestionFlowErrorsFileThenReturnFileResource(boolean isAdmin) {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    Path organizationBasePath = Path.of("/organizationFolder");
    String filePathName = "examplePath";
    String fileName = "testFile.zip";
    String discardFileName = "errorFile.zip";
    Path fullFilePath = organizationBasePath.resolve(filePathName).resolve(ERRORS_SUB_FOLDER).resolve(discardFileName);

    UserInfo user = isAdmin ? TestUtils.getSampleAdminUser() : TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);
    ingestionFlowFile.setDiscardFileName(discardFileName);
    if (!isAdmin) {
      ingestionFlowFile.setOperatorExternalId(user.getMappedExternalUserId());
    }
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    InputStream decryptedInputStream = mock(ByteArrayInputStream.class);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);
    when(fileStorerServiceMock.getUploadedOrArchivedPath(organizationId, ERRORS_SUB_FOLDER, filePathName, discardFileName))
      .thenReturn(fullFilePath.resolve(discardFileName));
    when(fileStorerServiceMock.decryptFile(fullFilePath, discardFileName))
      .thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, user, accessToken);

    assertNotNull(result);
    assertEquals(discardFileName, result.getFileName());

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenUnauthorizedOrganizationWhenDownloadIngestionFlowErrorsFileThenThrowAuthorizationDeniedException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(-1L);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(AuthorizationDeniedException.class, () -> ingestionFlowFileService.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(UnauthorizedFileDownloadException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenUnauthorizedUserWhenDownloadIngestionFlowErrorsFileThenThrowUnauthorizedFileDownloadException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(1L);
    ingestionFlowFile.setOperatorExternalId("OTHERUSER");
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(UnauthorizedFileDownloadException.class, () -> ingestionFlowFileService.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowFileNotFoundWhenDownloadIngestionFlowFileThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(null);

    Assertions.assertThrows(FileNotFoundException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowFileNotFoundWhenDownloadIngestionFlowErrorsFileThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(null);

    Assertions.assertThrows(FileNotFoundException.class, () -> ingestionFlowFileService.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowDiscardFileNullWhenDownloadIngestionFlowErrorsFileThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setOrganizationId(1L);
    ingestionFlowFile.setOperatorExternalId("OTHERUSER");
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.COMPLETED);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(FileNotFoundException.class, () -> ingestionFlowFileService.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    InputStream decryptedInputStream = mock(ByteArrayInputStream.class);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);
    when(fileStorerServiceMock.getUploadedOrArchivedPath(organizationId, ARCHIVED_SUB_FOLDER, filePathName, fileName))
      .thenReturn(fullFilePath.resolve(fileName));
    when(fileStorerServiceMock.decryptFile(fullFilePath, fileName))
      .thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken);

    assertNotNull(result);
    assertEquals(fileName, result.getFileName());
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenNullIngestionFlowFileThenCheckCondition() {
    givenIngestionFlowFileThenThrowsIngestionFlowFileNotFoundException(null);
  }

  @Test
  void givenUploadedIngestionFlowFileThenCheckCondition() {
    givenIngestionFlowFileThenThrowsIngestionFlowFileNotFoundException(
      new IngestionFlowFile()
        .status(IngestionFlowFileStatus.UPLOADED)
        .fileOrigin(String.valueOf(FileOrigin.SIL)));
  }

  @Test
  void givenWrongOriginIngestionFlowFileThenCheckCondition() {
    givenIngestionFlowFileThenThrowsIngestionFlowFileNotFoundException(
      new IngestionFlowFile()
        .status(IngestionFlowFileStatus.WAITING_FILE)
        .fileOrigin(String.valueOf(FileOrigin.PAGOPA)));
  }

  @Test
  void whenDownloadNoticeThenOk() throws IOException {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String fileName = "file.zip";
    String filePathName = "examplePath";
    String noticeFileName = "file_notice.zip";
    Path organizationBasePath = Path.of("/organizationFolder");
    Path archiveFolderPath = organizationBasePath.resolve(filePathName).resolve(ARCHIVED_SUB_FOLDER);

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);

    byte[] fileContent = "test content".getBytes();
    InputStream decryptedInputStream = new ByteArrayInputStream(fileContent);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    when(fileStorerServiceMock.getUploadedOrArchivedPath(organizationId, ARCHIVED_SUB_FOLDER, filePathName, noticeFileName))
      .thenReturn(archiveFolderPath.resolve(noticeFileName));

    when(fileStorerServiceMock.decryptFile(archiveFolderPath, noticeFileName))
      .thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadNotice(organizationId, ingestionFlowFileId, user, accessToken);

    assertNotNull(result);
    assertEquals(noticeFileName, result.getFileName());
    assertArrayEquals(fileContent, result.getResourceStream().getContentAsByteArray());
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowFileNotFoundWhenDownloadNoticeThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(null);

    Assertions.assertThrows(FileNotFoundException.class, () ->
      ingestionFlowFileService.downloadNotice(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void whenDownloadIuvFileThenOk() throws IOException {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String fileName = "file.zip";
    String filePathName = "examplePath";
    String iuvFileName = "file_iuv.zip";
    Path organizationBasePath = Path.of("/organizationFolder");
    Path archiveFolderPath = organizationBasePath.resolve(filePathName).resolve(ARCHIVED_SUB_FOLDER);

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setIngestionFlowFileType(IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName(filePathName);

    byte[] fileContent = "test content".getBytes();
    InputStream decryptedInputStream = new ByteArrayInputStream(fileContent);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    when(fileStorerServiceMock.getUploadedOrArchivedPath(organizationId, ARCHIVED_SUB_FOLDER, filePathName, iuvFileName))
      .thenReturn(archiveFolderPath.resolve(iuvFileName));

    when(fileStorerServiceMock.decryptFile(archiveFolderPath, iuvFileName))
      .thenReturn(decryptedInputStream);

    FileResourceDTO result = ingestionFlowFileService.downloadIuvFile(organizationId, ingestionFlowFileId, user, accessToken);

    assertNotNull(result);
    assertEquals(iuvFileName, result.getFileName());
    assertArrayEquals(fileContent, result.getResourceStream().getContentAsByteArray());
    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenTypeNotDPInstallmentsWhenDownloadIuvFileThenThrowInvalidFileTypeException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String fileName = "file.zip";

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setIngestionFlowFileType(IngestionFlowFile.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setFilePathName("examplePath");

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    InvalidFileTypeException exception = assertThrows(InvalidFileTypeException.class, () ->
      ingestionFlowFileService.downloadIuvFile(organizationId, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    assertEquals("INVALID_FILE_TYPE", exception.getCode());
    assertEquals("It's not possible to download IUV file for ingestionFlowFileId: 10. Expected type: DP_INSTALLMENTS, found: DEBT_POSITIONS_TYPE", exception.getMessage());
  }

  void givenIngestionFlowFileThenThrowsIngestionFlowFileNotFoundException(IngestionFlowFile ingestionFlowFile) {
    Long ingestionFlowFileId = 1L;
    String accessToken = "TOKEN";
    long organizationId = 1L;
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    UserInfo user = TestUtils.getSampleUser();

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    Assertions.assertThrows(IngestionFlowFileNotFoundException.class, () -> ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.ORGANIZATIONS_SIL_SERVICE, FileOrigin.SIL,
      fileName, file, ingestionFlowFileId, user, accessToken));

    verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
  }
}
