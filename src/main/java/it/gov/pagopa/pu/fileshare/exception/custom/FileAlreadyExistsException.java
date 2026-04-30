package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileAlreadyExistsException extends BaseBusinessException {
  public FileAlreadyExistsException(String code, String message) {
    super(code, message);
  }

}
