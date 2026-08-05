package it.gov.pagopa.pu.fileshare.util;


import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UtilitiesTest {

  public static void setTraceId(String traceId) {
    setTraceId(traceId, null);
  }
  public static void setTraceId(String traceId, String spanId) {
    MDC.put("traceId", traceId);
    MDC.put("spanId", spanId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  @Test
  void testGetSpanId(){
    // Given
    String expectedResult = "SPANID";
    setTraceId("TRACEID", expectedResult);

    // When
    String result = Utilities.getSpanId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
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
        Utilities.getExclusivePresenceOrThrow(fileA, fileB));
      assertEquals("[INVALID_FILES] Exactly one of the two files must be non-null", ex.getMessage());
    } else {
      MultipartFile result = Utilities.getExclusivePresenceOrThrow(fileA, fileB);
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
