package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.InvalidFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
  private FileService fileService;
  private static final String VALID_FILE_EXTENSION = ".zip";

  @BeforeEach
  void setUp() {
    fileService = new FileService();
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
}
