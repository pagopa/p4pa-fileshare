package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class UnauthorizedFileDownloadException extends BaseBusinessException {
  public UnauthorizedFileDownloadException(String code, String message) {
    super(code, message);
  }
}
