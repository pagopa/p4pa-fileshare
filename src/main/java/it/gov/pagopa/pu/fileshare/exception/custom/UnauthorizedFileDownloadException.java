package it.gov.pagopa.pu.fileshare.exception.custom;

public class UnauthorizedFileDownloadException extends BaseBusinessException {
  public UnauthorizedFileDownloadException(String code, String message) {
    super(code, message);
  }
}
