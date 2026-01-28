package it.gov.pagopa.pu.fileshare.exception.custom;

public class InvalidFileTypeException extends BaseBusinessException {
  public InvalidFileTypeException(String code, String message) {
    super(code, message);
  }
}
