package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.exception.custom.FileAlreadyExistsException;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
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
import java.util.Objects;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class DuplicateIngestionFlowFileRequestHandlerServiceTest {

  private static final String ARCHIVE_FOLDER = "archive";

  @Mock
  private IngestionFlowFileService ingestionFlowFileServiceMock;


  private final String accessToken = "ACCESS_TOKEN";
  private final Long organizationId = 1L;
  private final String filePathName = "FILE_PATH";

  private final String originalFileExtension = ".zip.cipher";

  private final String discardFileExtension = ".csv.cipher";

  private final String alreadyArchivedFileExtension = ".csv.zip.cipher";

  @TempDir
  private Path tempDir;
  private Path filePath;
  private Path archivedPath;
  private Path originalPositionFile;

  private DuplicateIngestionFlowFileRequestHandlerService service;

  @BeforeEach
  void init() throws IOException {
    Path sharedFolder = tempDir.resolve("shared");
    filePath = sharedFolder
      .resolve(organizationId + "")
      .resolve(filePathName);
    Files.createDirectories(filePath);
    archivedPath = filePath.resolve(ARCHIVE_FOLDER);

    originalPositionFile = Files.createTempFile(filePath, "fileNotArchived", originalFileExtension);

    FoldersPathsConfig foldersPathsConfig = new FoldersPathsConfig();
    foldersPathsConfig.setShared(sharedFolder.toString());
    FileStorerService fileStorerService = new FileStorerService(foldersPathsConfig, null);

    service = new DuplicateIngestionFlowFileRequestHandlerService(ingestionFlowFileServiceMock, fileStorerService);
  }

  private Path createAlreadyArchivedFile() throws IOException {
    Files.createDirectory(archivedPath);
    return Files.createTempFile(archivedPath, "fileAlreadyArchived", alreadyArchivedFileExtension);
  }

  private Path createDiscardFile() throws IOException {
    return Files.createTempFile(filePath, "ERRORS_fileNotArchived", discardFileExtension);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      ingestionFlowFileServiceMock
    );
  }

  //region UnknownIngestionFlowFile
  @Test
  void givenUnknownIngestionFlowFileAndNotExistentFileWhenHandleDuplicateFileThenRenameIt() {
    // Given
    String fileName = "NOT_EXISTENT_FILE";

    Mockito.when(ingestionFlowFileServiceMock.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken))
      .thenReturn(null);

    // When, Then
    Assertions.assertDoesNotThrow(() -> service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken));
  }

  @Test
  void givenUnknownIngestionFlowFileAndNotArchivedFileWhenHandleDuplicateFileThenRenameIt() throws IOException {
    testUnknownIngestionFlowFile(originalPositionFile, originalFileExtension);
  }

  @Test
  void givenUnknownIngestionFlowFileAndAlreadyArchivedFileWhenHandleDuplicateFileThenRenameIt() throws IOException {
    Path alreadyArchivedFilePath = createAlreadyArchivedFile();
    testUnknownIngestionFlowFile(alreadyArchivedFilePath, alreadyArchivedFileExtension);
  }

  private void testUnknownIngestionFlowFile(Path filePath, String fileExtension) throws IOException {
    // Given
    String fileNameNoExtension = filePath.getFileName().toString().replace(fileExtension, "");
    String fileName = filePath.getFileName().toString().replace(".cipher", "");

    Mockito.when(ingestionFlowFileServiceMock.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken))
      .thenReturn(null);

    long previousMillis = System.currentTimeMillis();

    // When
    service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken);

    // Then
    assertUnknownIngestionFlowFile(previousMillis, filePath, fileNameNoExtension, fileExtension);
  }

  private void assertUnknownIngestionFlowFile(long previousMillis, Path filePath, String fileNameNoPrefix, String fileExtension) throws IOException {
    Assertions.assertFalse(Files.exists(filePath));
    String expectedArchivedFilePrefix = fileNameNoPrefix + "_UNKNOWN_";
    try (Stream<Path> archivedFiles = Files.list(archivedPath)) {
      Assertions.assertEquals(
        1,
        archivedFiles
          .filter(p -> p.getFileName().toString().startsWith(expectedArchivedFilePrefix))
          .filter(p -> {
            long millis = Long.parseLong(
              p.getFileName().toString()
                .replace(expectedArchivedFilePrefix, "")
                .replace(fileExtension, "")
            );
            return previousMillis <= millis;
          })
          .count()
      );
    }
  }
//endregion

  //region KnownIngestionFlowFile
  @Test
  void givenKnownIngestionFlowFileNotErrorWhenHandleDuplicateFileThenRenameIt() {
    // Given
    String fileName = "NOT_EXISTENT_FILE";
    long ingestionFlowFileId = System.currentTimeMillis();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(ingestionFlowFileId);

    Mockito.when(ingestionFlowFileServiceMock.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken))
      .thenReturn(ingestionFlowFile);

    // When, Then
    Assertions.assertThrows(FileAlreadyExistsException.class, () -> service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken));
  }

  @Test
  void givenKnownIngestionFlowFileAndNotExistentFileAndNotDiscardFileNameWhenHandleDuplicateFileThenRenameIt() {
    // Given
    String fileName = "NOT_EXISTENT_FILE";
    long ingestionFlowFileId = System.currentTimeMillis();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(ingestionFlowFileId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.ERROR);
    ingestionFlowFile.setFileName(fileName);

    Mockito.when(ingestionFlowFileServiceMock.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken))
      .thenReturn(ingestionFlowFile);

    Mockito.when(ingestionFlowFileServiceMock.updateFileNames(ingestionFlowFileId,
        buildExpectedNewName(fileName, ingestionFlowFileId),
        null,
        accessToken))
      .thenReturn(0);

    // When, Then
    Assertions.assertDoesNotThrow(() -> service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken));
  }

  @Test
  void givenKnownIngestionFlowFileAndNotArchivedFileWhenHandleDuplicateFileThenRenameIt() throws IOException {
    testKnownIngestionFlowFile(originalPositionFile, originalFileExtension);
  }

  @Test
  void givenKnownIngestionFlowFileAndAlreadyArchivedFileWhenHandleDuplicateFileThenRenameIt() throws IOException {
    Path alreadyArchivedFilePath = createAlreadyArchivedFile();
    testKnownIngestionFlowFile(alreadyArchivedFilePath, alreadyArchivedFileExtension);
  }

  private void testKnownIngestionFlowFile(Path filePath, String fileExtension) throws IOException {
    // Given
    String fileNameNoExtension = filePath.getFileName().toString().replace(fileExtension, "");
    String fileName = filePath.getFileName().toString().replace(".cipher", "");
    long ingestionFlowFileId = System.currentTimeMillis();
    Path discardFilePath = createDiscardFile();

    IngestionFlowFile ingestionFlowFile = new IngestionFlowFile();
    ingestionFlowFile.setIngestionFlowFileId(ingestionFlowFileId);
    ingestionFlowFile.setStatus(IngestionFlowFileStatus.ERROR);
    ingestionFlowFile.setFileName(fileName);
    ingestionFlowFile.setDiscardFileName(discardFilePath.getFileName().toString().replace(".cipher", ""));

    Mockito.when(ingestionFlowFileServiceMock.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken))
      .thenReturn(ingestionFlowFile);

    Mockito.when(ingestionFlowFileServiceMock.updateFileNames(ingestionFlowFileId,
        buildExpectedNewName(fileName, ingestionFlowFileId),
        buildExpectedNewName(Objects.requireNonNull(ingestionFlowFile.getDiscardFileName()), ingestionFlowFileId),
        accessToken))
      .thenReturn(0);

    // When
    service.handleDuplicateFile(organizationId, ARCHIVE_FOLDER, filePathName, fileName, accessToken);

    // Then
    assertKnownIngestionFlowFile(ingestionFlowFileId, filePath, fileNameNoExtension, fileExtension, discardFilePath);
  }

  private String buildExpectedNewName(String fileName, long ingestionFlowFileId){
    return fileName.replaceFirst("(\\..*)$", "_ERROR_" + ingestionFlowFileId + "$1");
  }

  private void assertKnownIngestionFlowFile(long ingestionFlowFileId, Path filePath, String fileNameNoPrefix, String fileExtension, Path discardFilePath) {
    Assertions.assertFalse(Files.exists(filePath));
    Assertions.assertTrue(Files.exists(archivedPath.resolve(fileNameNoPrefix + "_ERROR_" + ingestionFlowFileId + fileExtension)));

    Assertions.assertFalse(Files.exists(discardFilePath));
    Assertions.assertTrue(Files.exists(archivedPath.resolve(discardFilePath.getFileName().toString().replace(discardFileExtension, "") + "_ERROR_" + ingestionFlowFileId + discardFileExtension)));
  }
//endregion
}
