package it.gov.pagopa.pu.fileshare.connector.auth.client;

import it.gov.pagopa.pu.fileshare.connector.auth.config.AuthApisHolder;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.p4paauth.controller.generated.AuthnApi;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthnClientTest {
  @Mock
  private AuthApisHolder authApisHolderMock;
  @Mock
  private AuthnApi authnApiMock;

  private AuthnClient authnClient;

  @BeforeEach
  void setUp() {
    authnClient = new AuthnClient(authApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      authApisHolderMock
    );
  }

  @Test
  void whenGetUserInfoThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    UserInfo expectedResult = new UserInfo();

    Mockito.when(authApisHolderMock.getAuthnApi(accessToken))
      .thenReturn(authnApiMock);
    Mockito.when(authnApiMock.getUserInfo())
      .thenReturn(expectedResult);

    // When
    UserInfo result = authnClient.getUserInfo(accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenUnauthorizedExceptionWhenGetUserInfoThenThrowInvalidAccessTokenException() {
    String accessToken = "ACCESSTOKEN";
    String bodyMessage = "bodyMessage";

    when(authApisHolderMock.getAuthnApi(accessToken))
      .thenReturn(authnApiMock);
    when(authnApiMock.getUserInfo())
      .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, bodyMessage.getBytes(), null));

    InvalidAccessTokenException exception = Assertions.assertThrows(InvalidAccessTokenException.class, () -> authnClient.getUserInfo(accessToken));

    assertEquals("[INVALID_ACCESS_TOKEN] %s".formatted(bodyMessage), exception.getMessage());
  }
}
