package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

@ExtendWith(MockitoExtension.class)
class FileStorerServiceTest {
  private FileStorerService fileStorerService;
  @Mock
  private FoldersPathsConfig foldersPathsConfig;
  private static final String FILE_ENCRYPT_PASSWORD = "testPassword";

  @BeforeEach
  void setUp() {
    fileStorerService = new FileStorerService(foldersPathsConfig,FILE_ENCRYPT_PASSWORD);
  }

  @Test
  void givenInvalidFileWhenSaveToSharedFolderThenFileUploadException() {
    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(
      AESUtils.class);
      MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(
        Files.class)) {
      try {
        fileStorerService.saveToSharedFolder(null, "");
        Assertions.fail("Expected FileUploadException");
      } catch (FileUploadException e) {
        Mockito.verifyNoInteractions(foldersPathsConfig);
        aesUtilsMockedStatic.verifyNoInteractions();
        filesMockedStatic.verifyNoInteractions();
      }
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

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(
      AESUtils.class);
      MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(
        Files.class)) {
      try {
        fileStorerService.saveToSharedFolder(file, "");
        Assertions.fail("Expected InvalidFileException");
      } catch (InvalidFileException e) {
        Mockito.verifyNoInteractions(foldersPathsConfig);
        aesUtilsMockedStatic.verifyNoInteractions();
        filesMockedStatic.verifyNoInteractions();
      }
    }
  }

  @Test
  void givenErrorWhenSaveToSharedFolderThenFileUploadException() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(
      AESUtils.class);
      MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(
        Files.class)) {
      String sharedFolderPath = "/shared";
      Mockito.when(foldersPathsConfig.getShared()).thenReturn(sharedFolderPath);
      Mockito.when(AESUtils.encrypt(Mockito.eq(FILE_ENCRYPT_PASSWORD), (InputStream) Mockito.any()))
        .thenThrow(new RuntimeException());

      try {
        fileStorerService.saveToSharedFolder(file, "");
        Assertions.fail("Expected FileUploadException");
      } catch (FileUploadException e) {
        Mockito.verify(foldersPathsConfig).getShared();
        Mockito.verifyNoMoreInteractions(foldersPathsConfig);
        aesUtilsMockedStatic.verify(() -> AESUtils.encrypt(Mockito.anyString(),(InputStream) Mockito.any()));
        aesUtilsMockedStatic.verifyNoMoreInteractions();
        filesMockedStatic.verify(() -> Files.exists(Mockito.any()));
        filesMockedStatic.verify(() -> Files.createDirectories(Mockito.any()));
        filesMockedStatic.verifyNoMoreInteractions();
      }
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

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(
      AESUtils.class);
      MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(
        Files.class)
    ) {
      String sharedFolderPath = "/shared";
      Mockito.when(foldersPathsConfig.getShared()).thenReturn(sharedFolderPath);
      String relativePath = "/relative";

      String result = fileStorerService.saveToSharedFolder(file, relativePath);

      Assertions.assertEquals(Paths.get(relativePath,filename).toString(),result);
      Mockito.verify(foldersPathsConfig).getShared();
      Mockito.verifyNoMoreInteractions(foldersPathsConfig);
      aesUtilsMockedStatic.verify(() -> AESUtils.encrypt(Mockito.anyString(),(InputStream) Mockito.any()));
      aesUtilsMockedStatic.verifyNoMoreInteractions();
      filesMockedStatic.verify(() -> Files.exists(Mockito.eq(
        Paths.get(sharedFolderPath,relativePath))));
      filesMockedStatic.verify(() -> Files.createDirectories(Mockito.eq(
        Paths.get(sharedFolderPath,relativePath))));
      filesMockedStatic.verify(() -> Files.copy((InputStream) Mockito.any(),Mockito.eq(Paths.get(sharedFolderPath,relativePath,filename)), Mockito.any()));
      filesMockedStatic.verifyNoMoreInteractions();
    }
  }
}
