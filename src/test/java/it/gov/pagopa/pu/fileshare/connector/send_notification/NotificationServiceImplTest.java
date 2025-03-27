package it.gov.pagopa.pu.fileshare.connector.send_notification;

import it.gov.pagopa.pu.fileshare.connector.send_notification.client.NotificationClient;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
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
  void whenGetSendNotificationThenInvokeClient(){
    // Given
    String accessToken = "access_token";
    String sendNotificationId = "sendNotificationId";
    SendNotificationDTO expectedResponse = new SendNotificationDTO();

    Mockito.when(notificationClientMock.getSendNotification(sendNotificationId,accessToken)).thenReturn(
      expectedResponse);

    // When
    SendNotificationDTO response = notificationService.getSendNotification(
      sendNotificationId, accessToken);

    // Then
    Assertions.assertSame(expectedResponse,response);
  }

  @Test
  void whenStartNotificationThenInvokeClient(){
    // Given
    LoadFileRequest loadFileRequest = new LoadFileRequest();
    String accessToken = "access_token";
    String sendNotificationId = "sendNotificationId";
    StartNotificationResponse expectedResponse = new StartNotificationResponse();

    Mockito.when(notificationClientMock.startNotification(sendNotificationId,loadFileRequest,accessToken)).thenReturn(
      expectedResponse);

    // When
    StartNotificationResponse response = notificationService.startNotification(
      sendNotificationId,loadFileRequest, accessToken);

    // Then
    Assertions.assertSame(expectedResponse,response);
  }

}
