package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileAlreadyExistsException extends RuntimeException {
  public FileAlreadyExistsException(String message) {
    super(message);
  }

}
