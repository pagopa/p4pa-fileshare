package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
  public static final List<String> VERSION_LIST = List.of("1.0", "1.1", "1.3", "1.4", "2.0");
  private FileService fileService;

  @BeforeEach
  void setUp() {
    fileService = new FileService();
  }

  @Test
  void givenValidFileExtensionWhenValidateFileThenOk(){
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    fileService.validateFile(file);
  }

  @Test
  void givenNoFileWhenValidateFileThenInvalidFileException(){
    try{
      fileService.validateFile(null);
      Assertions.fail("Expected InvalidFileException");
    }catch(InvalidFileException e){
      //do nothing
    }
  }

  @Test
  void givenInvalidFilenameWhenValidateFileThenInvalidFileException(){
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "../test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    try{
      fileService.validateFile(file);
      Assertions.fail("Expected InvalidFileException");
    }catch(InvalidFileException e){
      //do nothing
    }
  }

  @Test
  void whenValidateVersionFromIngestionFlowFilenameThenOk(){
    String fileName = "fileName2_0.txt";

    String version = fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName);

    assertEquals("2.0", version);
  }

  @Test
  void givenEngVersionWhenValidateVersionFromIngestionFlowFilenameThenOk(){
    String fileName = "fileName2_0-eng.txt";

    String version = fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName);

    assertEquals("2.0-eng", version);
  }

  @Test
  void givenInvalidFilenameWhenValidateVersionFromIngestionFlowFilenameThenInvalidFileException(){
    String fileName = "fileName.txt";

    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName));

    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_3, 1_4, 2_0]", ex.getMessage());
  }
}
