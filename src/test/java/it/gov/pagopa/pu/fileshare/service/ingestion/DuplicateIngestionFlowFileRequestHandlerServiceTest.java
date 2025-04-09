package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DuplicateIngestionFlowFileRequestHandlerServiceTest {

  private static final String ARCHIVE_FOLDER = "archive";

  @Mock
  private IngestionFlowFileService ingestionFlowFileServiceMock;
  @Mock
  private FileStorerService fileStorerServiceMock;

  private DuplicateIngestionFlowFileRequestHandlerService service;

  @BeforeEach
  void init(){
    service = new DuplicateIngestionFlowFileRequestHandlerService(ingestionFlowFileServiceMock, fileStorerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      ingestionFlowFileServiceMock,
      fileStorerServiceMock
    );
  }

  @Test
  void givenUnknownIngestionFlowFileAndNotExistentFileWhenHandleDuplicateFileThenRenameIt(){
    // Given
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;
    String filePathName = "FILE_PATH";
    String fileName = "FILE_NAME";

    Mockito.when(ingestionFlowFileServiceMock.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken))
      .thenReturn(null);

    // When
    service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken);
  }

  @Test
  void givenUnknownIngestionFlowFileAndNotArchivedFileWhenHandleDuplicateFileThenRenameIt(){

  }

  @Test
  void givenUnknownIngestionFlowFileAndAlreadyArchivedFileWhenHandleDuplicateFileThenRenameIt(){

  }
}
