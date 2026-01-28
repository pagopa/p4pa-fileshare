package it.gov.pagopa.pu.fileshare.exception.custom;

public class IngestionFlowFileNotFoundException extends BaseBusinessException {
  public IngestionFlowFileNotFoundException(String code, String message) {
    super(code, message);
  }
}
