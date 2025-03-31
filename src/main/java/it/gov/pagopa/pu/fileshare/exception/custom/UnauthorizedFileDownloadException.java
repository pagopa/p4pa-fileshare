package it.gov.pagopa.pu.fileshare.exception.custom;

public class UnauthorizedFileDownloadException extends RuntimeException {
  public UnauthorizedFileDownloadException(String message) {
    super(message);
  }
}
