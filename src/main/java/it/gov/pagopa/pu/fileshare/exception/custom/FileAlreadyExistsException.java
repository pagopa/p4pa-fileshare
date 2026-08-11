package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class FileAlreadyExistsException extends BaseBusinessException {
  public FileAlreadyExistsException(String code, String message) {
    super(code, message);
  }

}
