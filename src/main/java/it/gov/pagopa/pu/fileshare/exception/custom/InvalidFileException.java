package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class InvalidFileException extends BaseBusinessException {
  public InvalidFileException(String code, String message) {
    super(code, message);
  }
}
