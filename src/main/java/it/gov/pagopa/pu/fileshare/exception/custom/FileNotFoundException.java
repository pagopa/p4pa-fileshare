package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileNotFoundException extends RuntimeException {
  public FileNotFoundException(String message) {
    super(message);
  }

}
