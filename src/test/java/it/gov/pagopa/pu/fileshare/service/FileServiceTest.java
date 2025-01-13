package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import java.io.InputStream;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
  private FileService fileService;
  private static final String VALID_FILE_EXTENSION = ".zip";
  private static final String SHARED_FOLDER_ROOT_PATH = "testPath";
  private static final String FILE_ENCRYPT_PASSWORD = "testPassword";

  @BeforeEach
  void setUp() {
    fileService = new FileService(SHARED_FOLDER_ROOT_PATH,FILE_ENCRYPT_PASSWORD);
  }

  @Test
  void givenValidFileExtensionWhenValidateFileThenOk(){
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test"+VALID_FILE_EXTENSION,
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    fileService.validateFile(file, VALID_FILE_EXTENSION);
  }

  @Test
  void givenInvalidFileExtensionWhenValidateFileThenInvalidFileException(){
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try{
      fileService.validateFile(file, VALID_FILE_EXTENSION);
      Assertions.fail("Expected InvalidFileException");
    }catch(InvalidFileException e){
      //do nothing
    }
  }

  @Test
  void givenInvalidFileWhenSaveToSharedFolderThenIllegalStateException() {
    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(
      AESUtils.class);
      MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(
        Files.class)) {
      try {
        fileService.saveToSharedFolder(null, "");
        Assertions.fail("Expected FileUploadException");
      } catch (FileUploadException e) {
        aesUtilsMockedStatic.verifyNoInteractions();
        filesMockedStatic.verifyNoInteractions();
      }
    }
  }

  @Test
  void givenErrorWhenSaveToSharedFolderThenIllegalStateException() {
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
      Mockito.when(AESUtils.encrypt(Mockito.eq(FILE_ENCRYPT_PASSWORD), (InputStream) Mockito.any()))
        .thenThrow(new RuntimeException());

      try {
        fileService.saveToSharedFolder(file, "");
        Assertions.fail("Expected FileUploadException");
      } catch (FileUploadException e) {
        aesUtilsMockedStatic.verify(() -> AESUtils.encrypt(Mockito.anyString(),(InputStream) Mockito.any()));
        aesUtilsMockedStatic.verifyNoMoreInteractions();
        filesMockedStatic.verify(() -> Files.createDirectories(Mockito.any()));
        filesMockedStatic.verifyNoMoreInteractions();
      }
    }
  }

  @Test
  void givenValidFileWhenSaveToSharedFolderThenOK() {
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try (MockedStatic<AESUtils> aesUtilsMockedStatic = Mockito.mockStatic(
      AESUtils.class);
      MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(
        Files.class)
      ) {
      fileService.saveToSharedFolder(file, "/test");

      aesUtilsMockedStatic.verify(() -> AESUtils.encrypt(Mockito.anyString(),(InputStream) Mockito.any()));
      aesUtilsMockedStatic.verifyNoMoreInteractions();
      filesMockedStatic.verify(() -> Files.createDirectories(Mockito.any()));
      filesMockedStatic.verify(() -> Files.copy((InputStream) Mockito.any(),Mockito.any(), Mockito.any()));
      filesMockedStatic.verifyNoMoreInteractions();
    }
  }
}
