package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class DuplicateIngestionFlowFileRequestHandlerServiceTest {

  private static final String ARCHIVE_FOLDER = "archive";

  @Mock
  private IngestionFlowFileService ingestionFlowFileServiceMock;

  @TempDir
  private Path tempDir;

  private DuplicateIngestionFlowFileRequestHandlerService service;

  @BeforeEach
  void init() throws IOException {
    Path testFolder = tempDir.resolve("shared");
    Files.createDirectories(testFolder);

    FoldersPathsConfig foldersPathsConfig = new FoldersPathsConfig();
    foldersPathsConfig.setShared(testFolder.toString());
    FileStorerService fileStorerService = new FileStorerService(foldersPathsConfig, null);

    service = new DuplicateIngestionFlowFileRequestHandlerService(ingestionFlowFileServiceMock, fileStorerService);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      ingestionFlowFileServiceMock
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
    Assertions.assertDoesNotThrow(() -> service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken));
  }

  @Test
  void givenUnknownIngestionFlowFileAndNotArchivedFileWhenHandleDuplicateFileThenRenameIt(){
    // TODO
  }

  @Test
  void givenUnknownIngestionFlowFileAndAlreadyArchivedFileWhenHandleDuplicateFileThenRenameIt(){
    // TODO
  }
}
