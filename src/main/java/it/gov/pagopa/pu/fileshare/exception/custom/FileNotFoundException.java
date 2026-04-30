package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileNotFoundException extends BaseBusinessException {
  public FileNotFoundException(String code, String message) {
    super(code, message);
  }
}
