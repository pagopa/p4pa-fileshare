package it.gov.pagopa.pu.fileshare.exception.custom;

public class SendNotificationOrganizationMissMatchException extends RuntimeException {
  public SendNotificationOrganizationMissMatchException(String message){
    super(message);
  }
}
