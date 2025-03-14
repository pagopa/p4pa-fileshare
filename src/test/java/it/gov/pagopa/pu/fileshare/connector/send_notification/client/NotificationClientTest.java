package it.gov.pagopa.pu.fileshare.connector.send_notification.client;

import it.gov.pagopa.pu.fileshare.connector.send_notification.config.SendNotificationApisHolder;
import it.gov.pagopa.pu.sendnotification.controller.generated.NotificationApi;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
  void whenStartNotificationThenInvokeWithAccessToken() {
    String accessToken = "ACCESSTOKEN";
    Long organizationId = 1L;
    String sendNotificationId = "sendNotificationId";
    LoadFileRequest request = new LoadFileRequest();
    StartNotificationResponse expectedResult = new StartNotificationResponse();

    when(sendNotificationApisHolderMock.getNotificationApi(accessToken))
      .thenReturn(notificationApiMock);
    when(notificationApiMock.startNotification(sendNotificationId,organizationId,request))
      .thenReturn(expectedResult);

    StartNotificationResponse result = notificationClient.startNotification(
      sendNotificationId,organizationId, request, accessToken);

    assertSame(expectedResult, result);
  }
}
