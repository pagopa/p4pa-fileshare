package it.gov.pagopa.pu.fileshare.exception.custom;

public class OrganizationMissMatchException extends BaseBusinessException {
  public OrganizationMissMatchException(String code, String message){
    super(code, message);
  }
}
