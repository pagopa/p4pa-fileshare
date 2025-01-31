package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileDecryptionException extends RuntimeException {
  public FileDecryptionException(String message) {
    super(message);
  }

}
