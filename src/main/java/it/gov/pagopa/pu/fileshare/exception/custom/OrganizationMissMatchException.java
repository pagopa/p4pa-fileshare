package it.gov.pagopa.pu.fileshare.exception.custom;

public class OrganizationMissMatchException extends RuntimeException {
  public OrganizationMissMatchException(String message){
    super(message);
  }
}
