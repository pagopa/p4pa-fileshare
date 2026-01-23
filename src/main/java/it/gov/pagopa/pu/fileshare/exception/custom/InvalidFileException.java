package it.gov.pagopa.pu.fileshare.exception.custom;

public class InvalidFileException extends BaseBusinessException {
  public InvalidFileException(String code, String message) {
    super(code, message);
  }
}
