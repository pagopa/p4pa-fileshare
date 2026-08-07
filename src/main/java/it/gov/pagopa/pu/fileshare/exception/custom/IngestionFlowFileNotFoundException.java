package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class IngestionFlowFileNotFoundException extends BaseBusinessException {
  public IngestionFlowFileNotFoundException(String code, String message) {
    super(code, message);
  }
}
