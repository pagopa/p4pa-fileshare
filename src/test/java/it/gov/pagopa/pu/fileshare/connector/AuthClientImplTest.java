package it.gov.pagopa.pu.fileshare.connector;

import it.gov.pagopa.pu.p4paauth.controller.ApiClient;
import it.gov.pagopa.pu.p4paauth.controller.generated.AuthnApi;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthClientImplTest {

  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  @Mock
  private AuthnApi authApiMock;

  private AuthClientImpl authClient;

  @Mock
  private RestTemplate restTemplateMock;

  private String baseUrl = "http://example.com"; // Set a default value for testing

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    ApiClient apiClient = new ApiClient(restTemplateMock);
    apiClient.setBasePath(baseUrl);
    authClient = new AuthClientImpl(restTemplateBuilderMock, baseUrl);
  }

  @Test
  void givenValidateTokenWhenValidTokenThenOk() {
    String accessToken = "test-token";
    UserInfo expectedUserInfo = new UserInfo();
    ResponseEntity<UserInfo> responseEntity = new ResponseEntity<>(expectedUserInfo,HttpStatus.OK);

    Mockito.when(restTemplateMock.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

    UserInfo actualUserInfo = authClient.validateToken(accessToken);

    assertEquals(expectedUserInfo, actualUserInfo);
  }

}
