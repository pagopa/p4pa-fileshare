package it.gov.pagopa.pu.fileshare.exception.custom;

public class InvalidFileTypeException extends RuntimeException {
  public InvalidFileTypeException(String message) {
    super(message);
  }
}
