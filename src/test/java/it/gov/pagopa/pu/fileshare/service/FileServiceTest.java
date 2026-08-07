package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
  public static final List<String> VERSION_LIST = List.of("1.0", "1.1", "1.3", "1.4", "2.0");
  public static final List<String> VERSION_RECEIPT_LIST = List.of("1.0", "1.1", "1.2", "1.3");
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
    assertThrows(InvalidFileException.class, () -> fileService.validateFile(null), "Expected InvalidFileException");
  }

  @Test
  void givenInvalidFilenameWhenValidateFileThenInvalidFileException(){
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "../test.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    assertThrows(InvalidFileException.class, () -> fileService.validateFile(file), "Expected InvalidFileException");
  }

  @Test
  void whenValidateVersionFromIngestionFlowFilenameThenOk(){
    String fileName = "fileName1234__2_0.txt";

    String version = fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS);

    assertEquals("2.0", version);
  }

  @Test
  void whenValidateVersionRECEIPTFromIngestionFlowFilenameThenOk(){
    String fileName = "fileName1_3.txt";

    String version = fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT);

    assertEquals("1.3", version);
  }

  @Test
  void givenEngVersionWhenValidateVersionFromIngestionFlowFilenameThenOk(){
    String fileName = "fileName2_0-eng.txt";

    String version = fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS);

    assertEquals("2.0-eng", version);
  }

  @Test
  void givenEngVersionAndRECEIPTWhenValidateVersionFromIngestionFlowFilenameThenOk(){
    String fileName = "fileName2_0-eng.txt";

    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_RECEIPT_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT));

    assertEquals("INVALID_FILE_NAME", ex.getCode());
    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_2, 1_3]", ex.getMessage());
  }

  @Test
  void givenInvalidFilenameWhenValidateVersionFromIngestionFlowFilenameThenInvalidFileException(){
    String fileName = "fileName.txt";

    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS));

    assertEquals("INVALID_FILE_NAME", ex.getCode());
    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_3, 1_4, 2_0]", ex.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "2_0fileName.txt",
    "fileName_2026_2_0.txt",
    "fileName_22_0.txt",
  })
  void givenUnknownVersionWhenValidateVersionFromIngestionFlowFilenameThenInvalidFileException(String fileName){
    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS));

    assertEquals("INVALID_FILE_NAME", ex.getCode());
    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_3, 1_4, 2_0]", ex.getMessage());
  }

  @Test
  void givenInvalidFilenameWhenValidateVersionRECEIPTFromIngestionFlowFilenameThenInvalidFileException(){
    String fileName = "fileName.txt";

    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_RECEIPT_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT));

    assertEquals("INVALID_FILE_NAME", ex.getCode());
    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_2, 1_3]", ex.getMessage());
  }
}
