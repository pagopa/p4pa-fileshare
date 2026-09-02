package it.gov.pagopa.pu.fileshare.connector.send_notification.client;

import it.gov.pagopa.pu.fileshare.connector.send_notification.config.SendNotificationApisHolder;
import it.gov.pagopa.pu.sendnotification.client.generated.NotificationApi;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationClientTest {

  @Mock
  private SendNotificationApisHolder sendNotificationApisHolderMock;
  @Mock
  private NotificationApi notificationApiMock;

  private NotificationClient notificationClient;

  @BeforeEach
  void setUp() {
    notificationClient = new NotificationClient(sendNotificationApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(sendNotificationApisHolderMock);
  }

  @Test
  void whenGetSendNotificationInvokeWithAccessToken() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "sendNotificationId";
    SendNotificationDTO expectedResult = new SendNotificationDTO();

    when(sendNotificationApisHolderMock.getNotificationApi(accessToken))
      .thenReturn(notificationApiMock);
    when(notificationApiMock.getSendNotification(sendNotificationId))
      .thenReturn(expectedResult);

    SendNotificationDTO result = notificationClient.getSendNotification(sendNotificationId, accessToken);

    assertSame(expectedResult, result);
  }

  @Test
  void whenStartNotificationThenInvokeWithAccessToken() {
    String accessToken = "ACCESSTOKEN";
    String sendNotificationId = "sendNotificationId";
    LoadFileRequest request = new LoadFileRequest();
    StartNotificationResponse expectedResult = new StartNotificationResponse();

    when(sendNotificationApisHolderMock.getNotificationApi(accessToken))
      .thenReturn(notificationApiMock);
    when(notificationApiMock.startNotification(sendNotificationId,request))
      .thenReturn(expectedResult);

    StartNotificationResponse result = notificationClient.startNotification(sendNotificationId, request, accessToken);

    assertSame(expectedResult, result);
  }
}
