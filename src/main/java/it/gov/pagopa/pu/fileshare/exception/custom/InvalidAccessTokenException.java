package it.gov.pagopa.pu.fileshare.exception.custom;

public class InvalidAccessTokenException extends BaseBusinessException {
  public InvalidAccessTokenException(String code, String message) {
    super(code, message);
  }
}
