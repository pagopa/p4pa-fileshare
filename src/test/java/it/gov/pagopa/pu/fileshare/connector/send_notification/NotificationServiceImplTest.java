package it.gov.pagopa.pu.fileshare.connector.send_notification;

import it.gov.pagopa.pu.fileshare.connector.send_notification.client.NotificationClient;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
  @Mock
  private NotificationClient notificationClientMock;
  private NotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService = new NotificationServiceImpl(notificationClientMock);
  }

  @Test
  void whenStartNotificationThenInvokeClient(){
    LoadFileRequest loadFileRequest = new LoadFileRequest();
    String accessToken = "access_token";
    Long organizationId = 1L;
    String sendNotificationId = "sendNotificationId";
    StartNotificationResponse expectedResponse = new StartNotificationResponse();
    Mockito.when(notificationClientMock.startNotification(sendNotificationId,organizationId,loadFileRequest,accessToken)).thenReturn(
      expectedResponse);

    StartNotificationResponse response = notificationService.startNotification(
      sendNotificationId,organizationId,loadFileRequest, accessToken);

    Assertions.assertSame(expectedResponse,response);
  }
}
