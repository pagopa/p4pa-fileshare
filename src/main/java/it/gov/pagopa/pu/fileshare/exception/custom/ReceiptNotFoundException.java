package it.gov.pagopa.pu.fileshare.exception.custom;

import it.gov.pagopa.pu.fileshare.exception.common.BaseBusinessException;

public class ReceiptNotFoundException extends BaseBusinessException {
  public ReceiptNotFoundException(String code, String message) {
    super(code, message);
  }
}
