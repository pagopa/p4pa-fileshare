package it.gov.pagopa.pu.fileshare.exception.custom;

public class IngestionFlowFileNotFoundException extends RuntimeException {
  public IngestionFlowFileNotFoundException(String message) {
    super(message);
  }
}
