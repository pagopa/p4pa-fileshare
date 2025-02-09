package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class FileStorerServiceTest {

  private FileStorerService fileStorerService;

  @Mock
  private FoldersPathsConfig foldersPathsConfig;

  private static final String FILE_ENCRYPT_PASSWORD = "testPassword";
  private final String sharedFolder = "build/tmp";

  @BeforeEach
  void setUp() {
    Mockito.when(foldersPathsConfig.getShared()).thenReturn(sharedFolder);
    fileStorerService = new FileStorerService(foldersPathsConfig, FILE_ENCRYPT_PASSWORD);
  }

  @Test
  void givenInvalidFileWhenSaveToSharedFolderThenFileUploadException() {
    Assertions.assertThrows(FileUploadException.class, () ->
        fileStorerService.saveToSharedFolder(0L, null, "", ""));
  }

  @Test
  void givenInvalidFilenameWhenSaveToSharedFolderThenInvalidFileException() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Assertions.assertThrows(InvalidFileException.class, () ->
      fileStorerService.saveToSharedFolder(0L, file, "", "../test.txt"));
  }

  @Test
  void givenErrorWhenSaveToSharedFolderThenFileUploadException() throws IOException {
    MockMultipartFile fileSpy = Mockito.spy(new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    ));
    long organizationId = 0L;
    String relativePath = "relative";
    String fileName = fileSpy.getOriginalFilename();

    InputStream inpustStreamMock = Mockito.mock(InputStream.class);
    Mockito.doReturn(inpustStreamMock)
      .when(fileSpy)
      .getInputStream();

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {
      aesUtilsMockedStatic.when(() -> AESUtils.encryptAndSave(FILE_ENCRYPT_PASSWORD,
          inpustStreamMock,
          Path.of(sharedFolder).resolve(organizationId+"").resolve(relativePath),
          fileName))
        .thenThrow(new RuntimeException());

      Assertions.assertThrows(FileUploadException.class, () ->
        fileStorerService.saveToSharedFolder(organizationId, fileSpy, relativePath, fileName));
    }
  }

  @Test
  void givenValidFileWhenSaveToSharedFolderThenOK() throws IOException {
    MockMultipartFile fileSpy = Mockito.spy(new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    ));
    long organizationId = 0L;
    String relativeFilePath = "relative";
    String fileName = fileSpy.getOriginalFilename();

    InputStream inpustStreamMock = Mockito.mock(InputStream.class);
    Mockito.doReturn(inpustStreamMock)
      .when(fileSpy)
      .getInputStream();

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {

      String result = fileStorerService.saveToSharedFolder(organizationId, fileSpy, relativeFilePath, fileName);

      Assertions.assertEquals(relativeFilePath, result);
      aesUtilsMockedStatic.verify(() -> AESUtils.encryptAndSave(FILE_ENCRYPT_PASSWORD,
        inpustStreamMock,
        Path.of(sharedFolder).resolve(organizationId+"").resolve(relativeFilePath),
        fileName));
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
      fileStorerService.saveToSharedFolder(0L, file, "relative/../../test", ""));
  }

  @Test
  void givenExistingFileWhenDecryptFileThenReturnInputStreamResource() throws IOException {
    InputStream cipherInputStream = Mockito.mock(ByteArrayInputStream.class);
    Path filePath = Path.of("build");
    String fileName = "fileName";

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(AESUtils.class)) {
      aesUtilsMockedStatic.when(() -> AESUtils.decrypt(Mockito.eq(FILE_ENCRYPT_PASSWORD), Mockito.eq(filePath), Mockito.eq(fileName)))
        .thenReturn(cipherInputStream);

      try (InputStream result = fileStorerService.decryptFile(filePath, fileName)) {

        Assertions.assertNotNull(result);
        Assertions.assertSame(cipherInputStream, result);
      }
    }
  }
}




