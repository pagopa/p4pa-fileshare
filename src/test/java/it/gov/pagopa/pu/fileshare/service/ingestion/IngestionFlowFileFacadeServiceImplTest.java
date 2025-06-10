package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.pagopapayments.PrintPaymentNoticeService;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.IngestionFlowFileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
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
import it.gov.pagopa.pu.pagopapayments.dto.generated.SignedUrlResultDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  @Mock
  private PrintPaymentNoticeService printPaymentNoticeServiceMock;

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
      duplicateIngestionFlowFileRequestHandlerServiceMock,
      printPaymentNoticeServiceMock);
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
      duplicateIngestionFlowFileRequestHandlerServiceMock,
      printPaymentNoticeServiceMock);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk(boolean alreadyUploaded) {
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

    when(foldersPathsConfigMock.getIngestionFlowFilePath(IngestionFlowFileType.RECEIPT))
      .thenReturn(receiptFilePath);
    when(fileStorerServiceMock.checkIfAlreadyUploadedOrArchived(organizationId, ARCHIVED_SUB_FOLDER, receiptFilePath, fileName))
      .thenReturn(alreadyUploaded);
    when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(saveFileResult);
    when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(null, file,
      IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA, organizationId, filePath, null))
      .thenReturn(ingestionFlowFileRequestDTO);
    when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT, FileOrigin.PAGOPA,
      fileName, file, null, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    Mockito.verify(fileServiceMock).validateFile(file);
    if (alreadyUploaded) {
      Mockito.verify(duplicateIngestionFlowFileRequestHandlerServiceMock)
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
    when(fileServiceMock.validateVersionFromIngestionFlowFilename(versionList, fileName))
      .thenReturn(fileVersion);
    when(fileStorerServiceMock.saveToSharedFolder(organizationId, file, receiptFilePath, fileName))
      .thenReturn(saveFileResult);
    when(ingestionFlowFileDTOMapperMock.mapToIngestionFlowFileDTO(null, file,
      IngestionFlowFileType.DP_INSTALLMENTS, FileOrigin.PAGOPA, organizationId, filePath, fileVersion))
      .thenReturn(ingestionFlowFileRequestDTO);
    when(ingestionFlowFileServiceMock.createIngestionFlowFile(ingestionFlowFileRequestDTO, accessToken))
      .thenReturn(expectedIngestionFlowFileId);

    Long result = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.DP_INSTALLMENTS, FileOrigin.PAGOPA,
      fileName, file, null, TestUtils.getSampleUser(), accessToken);

    Assertions.assertSame(expectedIngestionFlowFileId, result);
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
    Mockito.verify(fileServiceMock).validateFile(file);

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
    Mockito.doThrow(new InvalidFileException("Invalid file version"))
      .when(fileServiceMock).validateVersionFromIngestionFlowFilename(versionList, fileName);

    try {
      ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.DP_INSTALLMENTS, FileOrigin.PAGOPA, fileName, file, null, TestUtils.getSampleUser(), accessToken);
    } catch (InvalidFileException e) {
      assertEquals("Invalid file version", e.getMessage());
    }
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
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

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(ingestionFlowFile);

    Assertions.assertThrows(UnauthorizedFileDownloadException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowFileNotFoundWhenDownloadIngestionFlowFileThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(null);

    Assertions.assertThrows(FileNotFoundException.class, () -> ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, user, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
  }

  @Test
  void givenIngestionFlowFileNotFoundWhenDownloadIngestionFlowErrorsFileThenThrowFileNotFoundException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;

    UserInfo user = TestUtils.getSampleUser();

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken)).thenReturn(null);

    Assertions.assertThrows(FileNotFoundException.class, () -> ingestionFlowFileService.downloadIngestionFlowErrorsFile(organizationId, ingestionFlowFileId, user, accessToken));

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
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
    String fileName = "notice.pdf";
    String pdfGeneratedId = "pdf123";
    String signedUrl = "http://example.com/notice.pdf";
    byte[] fileContent = "test content".getBytes();

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setPdfGeneratedId(pdfGeneratedId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);

    SignedUrlResultDTO signedUrlResultDTO = new SignedUrlResultDTO(null, List.of("notice"), signedUrl);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    when(printPaymentNoticeServiceMock.getSignedUrl(organizationId, ingestionFlowFile.getPdfGeneratedId(), accessToken))
      .thenReturn(signedUrlResultDTO);

    try (MockedConstruction<RestTemplate> ignored = Mockito.mockConstruction(RestTemplate.class,
      (mock, context) -> when(mock.getForEntity(signedUrl, byte[].class))
        .thenReturn(ResponseEntity.ok(fileContent)))) {

      FileResourceDTO result = ingestionFlowFileService.downloadNotice(organizationId, ingestionFlowFileId, user, accessToken);

      assertNotNull(result);
      assertEquals(fileName, result.getFileName());
      assertArrayEquals(fileContent, result.getResourceStream().getContentAsByteArray());
      Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    }
  }

  @Test
  void givenSignedUrlNullWhenDownloadNoticeThenThrowIllegalStateException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String fileName = "notice.pdf";
    String pdfGeneratedId = "pdf123";

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setPdfGeneratedId(pdfGeneratedId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);

    SignedUrlResultDTO signedUrlResultDTO = new SignedUrlResultDTO(null, null, null);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    when(printPaymentNoticeServiceMock.getSignedUrl(organizationId, ingestionFlowFile.getPdfGeneratedId(), accessToken))
      .thenReturn(signedUrlResultDTO);

    IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
      ingestionFlowFileService.downloadNotice(organizationId, ingestionFlowFileId, user, accessToken));

    assertEquals("Signed URL not available for ingestionFlowFileId: 10", exception.getMessage());
    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);

  }

  @Test
  void givenNoticeNullWhenDownloadNoticeThenThrowIllegalStateException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String fileName = "notice.pdf";
    String pdfGeneratedId = "pdf123";
    String signedUrl = "http://example.com/notice.pdf";

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setPdfGeneratedId(pdfGeneratedId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);

    SignedUrlResultDTO signedUrlResultDTO = new SignedUrlResultDTO(null, List.of("notice"), signedUrl);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    when(printPaymentNoticeServiceMock.getSignedUrl(organizationId, ingestionFlowFile.getPdfGeneratedId(), accessToken))
      .thenReturn(signedUrlResultDTO);

    try (MockedConstruction<RestTemplate> ignored = Mockito.mockConstruction(RestTemplate.class,
      (mock, context) -> when(mock.getForEntity(signedUrl, byte[].class))
        .thenReturn(ResponseEntity.ok(null)))) {

      IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
        ingestionFlowFileService.downloadNotice(organizationId, ingestionFlowFileId, user, accessToken));

      assertEquals("Downloaded file in the signed url: http://example.com/notice.pdf with ingestionFlowFileId: 10 is empty", exception.getMessage());
      Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    }
  }

  @Test
  void givenClientExceptionWhenDownloadNoticeThenThrowRestClientException() {
    String accessToken = "TOKEN";
    Long organizationId = 1L;
    Long ingestionFlowFileId = 10L;
    String fileName = "notice.pdf";
    String pdfGeneratedId = "pdf123";
    String signedUrl = "http://example.com/notice.pdf";

    UserInfo user = TestUtils.getSampleAdminUser();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(1L);
    ingestionFlowFile.setOrganizationId(organizationId);
    ingestionFlowFile.setPdfGeneratedId(pdfGeneratedId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.PROCESSING);
    ingestionFlowFile.setFileName(fileName);

    SignedUrlResultDTO signedUrlResultDTO = new SignedUrlResultDTO(null, List.of("notice"), signedUrl);

    when(ingestionFlowFileServiceMock.getIngestionFlowFile(ingestionFlowFileId, accessToken))
      .thenReturn(ingestionFlowFile);

    when(printPaymentNoticeServiceMock.getSignedUrl(organizationId, ingestionFlowFile.getPdfGeneratedId(), accessToken))
      .thenReturn(signedUrlResultDTO);

    try (MockedConstruction<RestTemplate> ignored = Mockito.mockConstruction(RestTemplate.class,
      (mock, context) -> when(mock.getForEntity(signedUrl, byte[].class))
        .thenThrow(new RestClientException("Error")))) {

      RestClientException exception = assertThrows(RestClientException.class, () ->
        ingestionFlowFileService.downloadNotice(organizationId, ingestionFlowFileId, user, accessToken));

      assertEquals("Error", exception.getMessage());
      Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, user, accessToken);
    }
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

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId, TestUtils.getSampleUser(), accessToken);
  }
}
