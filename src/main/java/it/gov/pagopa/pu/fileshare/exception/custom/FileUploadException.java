package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class FileUploadException extends BaseBusinessException {
  public FileUploadException(String code, String message) {
    super(code, message);
  }

  public FileUploadException(String code, String message, Throwable e) {
    super(code, message, e);
  }
}
