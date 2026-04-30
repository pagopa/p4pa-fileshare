package it.gov.pagopa.pu.fileshare.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErrorMessageParserTest {

  @ParameterizedTest
  @MethodSource("genericErrorCases")
  void givenMessageWhenParsingThenExpectedCodeAndDescription(
    String message,
    String expectedCode
  ) {
    ErrorMessageParser.ParsedError result = ErrorMessageParser.parse(message);

    assertEquals(expectedCode, result.code());
    assertNull(result.description());
  }

  private static Stream<Arguments> genericErrorCases() {
    return Stream.of(
      Arguments.of(null, "GENERIC_ERROR"),
      Arguments.of("   ", "GENERIC_ERROR"),
      Arguments.of("[INVALID_IBAN]", "INVALID_IBAN"),
      Arguments.of("[INVALID_IBAN]   ", "INVALID_IBAN")
    );
  }

  @Test
  void givenMessageWithoutBracketCodeThenGenericErrorAndTrimmedMessage() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("  plain error message  ");

    assertEquals("GENERIC_ERROR", result.code());
    assertEquals("plain error message", result.description());
  }

  @Test
  void givenBracketCodeAndDescriptionThenExtractBoth() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("[INVALID_IBAN] iban not valid");

    assertEquals("INVALID_IBAN", result.code());
    assertEquals("iban not valid", result.description());
  }

  @Test
  void givenLeadingSpacesBeforeBracketThenExtractCodeAndDescription() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("   [INVALID_IBAN] iban not valid");

    assertEquals("INVALID_IBAN", result.code());
    assertEquals("iban not valid", result.description());
  }

  @Test
  void givenEmptyBracketCodeThenFallbackToGenericError() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("[] something wrong");

    assertEquals("GENERIC_ERROR", result.code());
    assertEquals("[] something wrong", result.description());
  }

  @Test
  void givenBlankBracketCodeThenFallbackToGenericError() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("[   ] something wrong");

    assertEquals("GENERIC_ERROR", result.code());
    assertEquals("something wrong", result.description());
  }

  @Test
  void givenOpeningBracketWithoutClosingThenFallbackToGenericError() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("[INVALID_IBAN something wrong");

    assertEquals("GENERIC_ERROR", result.code());
    assertEquals("[INVALID_IBAN something wrong", result.description());
  }

  @Test
  void givenOnlyOpeningBracketThenFallbackToGenericError() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("[");

    assertEquals("GENERIC_ERROR", result.code());
    assertEquals("[", result.description());
  }

  @Test
  void givenBracketCodeAndDescriptionWithExtraSpacesThenTrimCorrectly() {
    ErrorMessageParser.ParsedError result =
      ErrorMessageParser.parse("[  INVALID_IBAN  ]   iban not valid   ");

    assertEquals("INVALID_IBAN", result.code());
    assertEquals("iban not valid", result.description());
  }
}
