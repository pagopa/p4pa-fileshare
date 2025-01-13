package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
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
  private IngestionFlowFileServiceImpl ingestionFlowFileService;
  private final String accessToken = "TOKEN";
  private final long organizationId = 1L;
  private static final String VALID_FILE_EXTENSION = ".zip";
  private static final String INGESTION_FLOW_FILE_PATH = "/test";

  @BeforeEach
  void setUp() {
    ingestionFlowFileService = new IngestionFlowFileServiceImpl(userAuthorizationServiceMock, fileServiceMock,VALID_FILE_EXTENSION,INGESTION_FLOW_FILE_PATH);
  }

  @Test
  void givenAuthorizedUserWhenUploadIngestionFlowFileThenOk(){
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test"+VALID_FILE_EXTENSION,
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    ingestionFlowFileService.uploadIngestionFlowFile(organizationId, IngestionFlowFileType.RECEIPT,
      file, TestUtils.getSampleUser(),accessToken);

    Mockito.verify(userAuthorizationServiceMock).checkUserAuthorization(organizationId,TestUtils.getSampleUser(),accessToken);
    Mockito.verify(fileServiceMock).validateFile(file,VALID_FILE_EXTENSION);
    Mockito.verify(fileServiceMock).saveToSharedFolder(file,INGESTION_FLOW_FILE_PATH);
  }

}
