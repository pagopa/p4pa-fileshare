package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
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
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
class FileStorerServiceTest {

  private FileStorerService fileStorerService;

  @Mock
  private FoldersPathsConfig foldersPathsConfig;

  private static final String FILE_ENCRYPT_PASSWORD = "testPassword";

  @BeforeEach
  void setUp() {
    Mockito.when(foldersPathsConfig.getShared()).thenReturn("/shared");
    fileStorerService = new FileStorerService(foldersPathsConfig, FILE_ENCRYPT_PASSWORD);
  }

  @Test
  void givenInvalidFileWhenSaveToSharedFolderThenFileUploadException() {
    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class);
         MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      Assertions.assertThrows(FileUploadException.class, () ->
        fileStorerService.saveToSharedFolder(0L, null, ""));
    }
  }

  @Test
  void givenInvalidFilenameWhenSaveToSharedFolderThenInvalidFileException() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "../test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Assertions.assertThrows(InvalidFileException.class, () ->
      fileStorerService.saveToSharedFolder(0L, file, ""));
  }

  @Test
  void givenErrorWhenSaveToSharedFolderThenFileUploadException() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class);
         MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      Mockito.when(AESUtils.encrypt(Mockito.eq(FILE_ENCRYPT_PASSWORD), (InputStream) Mockito.any()))
        .thenThrow(new RuntimeException());

      Assertions.assertThrows(FileUploadException.class, () ->
        fileStorerService.saveToSharedFolder(0L, file, "/relative"));
    }
  }

  @Test
  void givenValidFileWhenSaveToSharedFolderThenOK() {
    String filename = "test.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      filename,
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class);
         MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {

      String result = fileStorerService.saveToSharedFolder(0L, file, "/relative");

      Assertions.assertEquals(Paths.get("/relative", filename).toString(), result);
    }
  }

  @Test
  void givenInvalidPathWhenSaveToSharedFolderThenInvalidFileException() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Assertions.assertThrows(InvalidFileException.class, () ->
      fileStorerService.saveToSharedFolder(0L, file, "/relative/../../test"));
  }

  @Test
  void givenExistingFileWhenDecryptFileThenReturnInputStreamResource() {
    InputStream cipherInputStream = Mockito.mock(ByteArrayInputStream.class);

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {
      Mockito.when(AESUtils.decrypt(Mockito.anyString(), Mockito.any(InputStream.class)))
        .thenReturn(cipherInputStream);

      InputStream result = fileStorerService.decryptFile("src/test/resources/shared", "test.txt");

      Assertions.assertNotNull(result);
      Assertions.assertSame(cipherInputStream, result);
    }
  }

  @Test
  void givenNonExistingFileWhenDecryptFileThenFileNotFoundException() {
    Assertions.assertThrows(FileNotFoundException.class, () ->
      fileStorerService.decryptFile("src/test/resources/shared", "nonexistent.txt"));
  }
}




