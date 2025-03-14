package it.gov.pagopa.pu.fileshare.connector.send_notification.client;

import it.gov.pagopa.pu.fileshare.connector.send_notification.config.SendNotificationApisHolder;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationClient {

  private final SendNotificationApisHolder sendNotificationApisHolder;

  public NotificationClient(SendNotificationApisHolder sendNotificationApisHolder) {
    this.sendNotificationApisHolder = sendNotificationApisHolder;
  }

  public StartNotificationResponse startNotification(String sendNotificationId, Long organizationId, LoadFileRequest loadFileRequest, String accessToken) {
    return sendNotificationApisHolder.getNotificationApi(
        accessToken)
      .startNotification(sendNotificationId, organizationId,
        loadFileRequest);
  }
}
