package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class InvalidFileTypeException extends BaseBusinessException {
  public InvalidFileTypeException(String code, String message) {
    super(code, message);
  }
}
