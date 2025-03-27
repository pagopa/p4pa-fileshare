package it.gov.pagopa.pu.fileshare.connector.send_notification;

import it.gov.pagopa.pu.fileshare.connector.send_notification.client.NotificationClient;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
  private final NotificationClient notificationClient;

  public NotificationServiceImpl(NotificationClient notificationClient) {
    this.notificationClient = notificationClient;
  }

  @Override
  public SendNotificationDTO getSendNotification(String sendNotificationId, String accessToken) {
    return notificationClient.getSendNotification(sendNotificationId, accessToken);
  }

  @Override
  public StartNotificationResponse startNotification(String sendNotificationId, LoadFileRequest loadFileRequest, String accessToken) {
    return notificationClient.startNotification(sendNotificationId, loadFileRequest, accessToken);
  }
}
