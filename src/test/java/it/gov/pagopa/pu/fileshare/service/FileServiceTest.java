package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Stream;

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

    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_2, 1_3]", ex.getMessage());
  }

  @Test
  void givenInvalidFilenameWhenValidateVersionFromIngestionFlowFilenameThenInvalidFileException(){
    String fileName = "fileName.txt";

    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS));

    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_3, 1_4, 2_0]", ex.getMessage());
  }

  @Test
  void givenInvalidFilenameWhenValidateVersionRECEIPTFromIngestionFlowFilenameThenInvalidFileException(){
    String fileName = "fileName.txt";

    InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
      fileService.validateVersionFromIngestionFlowFilename(VERSION_RECEIPT_LIST, fileName, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT));

    assertEquals("File name must contain a valid version: [1_0, 1_1, 1_2, 1_3]", ex.getMessage());
  }

  @ParameterizedTest
  @MethodSource("provideFilesForExclusivePresenceTest")
  void givenFileParametersWhenGetExclusivePresenceOrThrowThenVerifyBehavior(
      MockMultipartFile fileA,
      MockMultipartFile fileB,
      boolean shouldThrow,
      MockMultipartFile expectedResult) {

    if (shouldThrow) {
      InvalidFileException ex = assertThrows(InvalidFileException.class, () ->
        fileService.getExclusivePresenceOrThrow(fileA, fileB));
      assertEquals("Exactly one of the two files must be non-null", ex.getMessage());
    } else {
      MultipartFile result = fileService.getExclusivePresenceOrThrow(fileA, fileB);
      assertEquals(expectedResult, result);
    }
  }

  private static Stream<Arguments> provideFilesForExclusivePresenceTest() {
    MockMultipartFile fileA = new MockMultipartFile(
      "fileA",
      "testA.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "contentA".getBytes()
    );

    MockMultipartFile fileB = new MockMultipartFile(
      "fileB",
      "testB.zip",
      MediaType.TEXT_PLAIN_VALUE,
      "contentB".getBytes()
    );

    return Stream.of(
      Arguments.of(fileA, null, false, fileA),  // fileA not null, fileB null -> returns fileA
      Arguments.of(null, fileB, false, fileB),  // fileA null, fileB not null -> returns fileB
      Arguments.of(fileA, fileB, true, null),   // both not null -> throws exception
      Arguments.of(null, null, true, null)      // both null -> throws exception
    );
  }

}
