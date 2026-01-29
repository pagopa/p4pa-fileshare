package it.gov.pagopa.pu.fileshare.exception.custom;

public class FileUploadException extends BaseBusinessException {
  public FileUploadException(String code, String message) {
    super(code, message);
  }

  public FileUploadException(String code, String message, Throwable e) {
    super(code, message, e);
  }
}
