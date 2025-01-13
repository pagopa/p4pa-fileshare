package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileUploadException extends RuntimeException {
  public FileUploadException(String message) {
    super(message);
  }
}
