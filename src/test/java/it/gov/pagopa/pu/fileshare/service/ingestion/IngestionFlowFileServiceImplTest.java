package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
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

}
