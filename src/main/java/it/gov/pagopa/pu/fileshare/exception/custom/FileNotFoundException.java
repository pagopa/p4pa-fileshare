package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class FileNotFoundException extends BaseBusinessException {
  public FileNotFoundException(String code, String message) {
    super(code, message);
  }
}
