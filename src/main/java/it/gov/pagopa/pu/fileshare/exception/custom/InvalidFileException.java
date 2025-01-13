package it.gov.pagopa.pu.fileshare.exception.custom;

public class InvalidFileException extends RuntimeException {
  public InvalidFileException(String message) {
    super(message);
  }
}
