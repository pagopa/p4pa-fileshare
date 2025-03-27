package it.gov.pagopa.pu.fileshare.connector.send_notification;

import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;

public interface NotificationService {
  SendNotificationDTO getSendNotification(String sendNotificationId, String accessToken);
  StartNotificationResponse startNotification(String sendNotificationId, LoadFileRequest loadFileRequest, String accessToken);
}
