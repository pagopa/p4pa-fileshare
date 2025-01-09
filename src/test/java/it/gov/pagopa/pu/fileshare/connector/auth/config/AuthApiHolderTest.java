package it.gov.pagopa.pu.fileshare.connector.auth.config;

import it.gov.pagopa.pu.fileshare.connector.BaseApiHolderTest;
import it.gov.pagopa.pu.p4paauth.controller.ApiClient;
import it.gov.pagopa.pu.p4paauth.dto.generated.AccessToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class AuthApiHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private AuthApisHolder authApisHolder;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    ApiClient apiClient = new ApiClient(restTemplateMock);
    String baseUrl = "http://example.com";
    apiClient.setBasePath(baseUrl);
    authApisHolder = new AuthApisHolder(baseUrl, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetAuthnApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> authApisHolder.getAuthnApi(accessToken)
        .postToken("clientId", "grantType", "scope", "subjectToken", "subjectIssuer", "subjectTokenType", "clientSecret"),
      AccessToken.class,
      authApisHolder::unload);
  }
}
