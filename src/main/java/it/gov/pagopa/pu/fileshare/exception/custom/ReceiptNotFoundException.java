package it.gov.pagopa.pu.fileshare.exception.custom;

public class ReceiptNotFoundException extends BaseBusinessException {
  public ReceiptNotFoundException(String code, String message) {
    super(code, message);
  }
}
