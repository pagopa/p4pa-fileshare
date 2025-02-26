package it.gov.pagopa.pu.fileshare.exception.custom;

public class FlowFileNotFoundException extends RuntimeException {
  public FlowFileNotFoundException(String message) {
    super(message);
  }
}
