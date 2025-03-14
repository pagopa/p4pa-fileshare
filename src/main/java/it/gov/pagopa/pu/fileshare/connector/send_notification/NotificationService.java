package it.gov.pagopa.pu.fileshare.connector.send_notification;

import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;

public interface NotificationService {
  StartNotificationResponse startNotification(String sendNotificationId, Long organizationId, LoadFileRequest loadFileRequest, String accessToken);
}
