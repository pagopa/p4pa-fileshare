package it.gov.pagopa.pu.fileshare.util;

import org.apache.commons.lang3.StringUtils;

public class ErrorMessageParser {
  private static final String GENERIC_ERROR = "GENERIC_ERROR";

  private ErrorMessageParser() {
  }

  public static ParsedError parse(String message) {
    if (StringUtils.isBlank(message)) {
      return new ParsedError(GENERIC_ERROR, null);
    }

    String s = message.stripLeading();
    if (!s.startsWith("[")) {
      return new ParsedError(GENERIC_ERROR, message.trim());
    }

    int end = s.indexOf(']');
    if (end <= 1) { // ']' not found or code empty "[]"
      return new ParsedError(GENERIC_ERROR, message.trim());
    }

    String code = s.substring(1, end).trim();
    if (StringUtils.isBlank(code)) {
      code = GENERIC_ERROR;
    }

    String description = s.substring(end + 1).trim();
    if (StringUtils.isBlank(description)) {
      description = null;
    }

    return new ParsedError(code, description);
  }

  public record ParsedError(String code, String description) { }
}
