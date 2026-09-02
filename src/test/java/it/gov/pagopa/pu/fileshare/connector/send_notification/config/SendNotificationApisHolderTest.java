package it.gov.pagopa.pu.fileshare.connector.send_notification.config;

import it.gov.pagopa.pu.fileshare.config.json.JsonConfig;
import it.gov.pagopa.pu.fileshare.connector.BaseApiHolderTest;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationApisHolderTest  extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private SendNotificationApisHolder apisHolder;
  private SendNotificationApiClientConfig apiClientConfig;

  @BeforeEach
  void setUp() {
    when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

    apiClientConfig = SendNotificationApiClientConfig.builder()
      .baseUrl("http://example.com")
      .maxAttempts(3)
      .build();
    apisHolder = new SendNotificationApisHolder(apiClientConfig, restTemplateBuilderMock, new JsonConfig().objectMapperJackson3());

    verifyHttpClientErrorJsonBodyHandlerConfiguration(apisHolder.getNotificationApi(null));
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void testRetryConfiguration() {
    assertRetry(apiClientConfig,
      accessToken -> apisHolder.getNotificationApi(accessToken)
        .startNotification("sendNotificationId",new LoadFileRequest()),
      new ParameterizedTypeReference<>() {}
    );
  }

  @Test
  void whenGetNotificationApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> apisHolder.getNotificationApi(accessToken)
        .startNotification("sendNotificationId",new LoadFileRequest()),
      new ParameterizedTypeReference<>() {},
      apisHolder::unload
    );
  }

}
