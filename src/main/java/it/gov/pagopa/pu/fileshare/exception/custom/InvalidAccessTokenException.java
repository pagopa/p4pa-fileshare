package it.gov.pagopa.pu.fileshare.exception.custom;

public class InvalidAccessTokenException extends RuntimeException {
  public InvalidAccessTokenException(String message) {
    super(message);
  }
}
