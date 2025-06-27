package it.gov.pagopa.pu.fileshare.exception.custom;

public class ReceiptNotFoundException extends RuntimeException {
  public ReceiptNotFoundException(String message) {
    super(message);
  }
}
