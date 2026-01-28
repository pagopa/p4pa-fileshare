package it.gov.pagopa.pu.fileshare.exception.custom;

import lombok.Getter;

@Getter
public class BaseBusinessException extends RuntimeException {
  protected final String code;

  protected BaseBusinessException(String code, String message) {
    super("[%s] %s".formatted(code, message));
    this.code = code;
  }

  protected BaseBusinessException(String code, String message, Throwable e) {
    super("[%s] %s".formatted(code, message), e);
    this.code = code;
  }
}
