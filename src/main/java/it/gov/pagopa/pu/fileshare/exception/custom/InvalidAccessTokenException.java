package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class InvalidAccessTokenException extends BaseBusinessException {
  public InvalidAccessTokenException(String code, String message) {
    super(code, message);
  }
}
