package it.gov.pagopa.pu.fileshare.service;

import static org.mockito.Mockito.mock;

import it.gov.pagopa.pu.fileshare.connector.auth.client.AuthnClient;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
class AuthorizationServiceTest {

  @Autowired
  private AuthorizationService authorizationService;

  @Mock
  private AuthnClient authClientImplMock;

  @BeforeEach
  void setUp(){
    authClientImplMock = mock(AuthnClient.class);
    authorizationService = new AuthorizationService(authClientImplMock);
  }

  @Test
  void givenValidAccessTokenWhenValidateTokenThenOk() {
    // When
    UserInfo ui = new UserInfo();
    Mockito.when(authClientImplMock.getUserInfo("ACCESSTOKEN")).thenReturn(ui);
    UserInfo result = authorizationService.validateToken("ACCESSTOKEN");

    // Then
    Assertions.assertEquals(
      ui,
      result
    );
  }

  @Test
  void givenInvalidAccessTokenWhenValidateTokenThenInvalidAccessTokenException() {
    // When
    Mockito.when(authClientImplMock.getUserInfo("INVALIDACCESSTOKEN")).thenThrow(new InvalidAccessTokenException("Bad Access Token provided"));
    InvalidAccessTokenException result = Assertions.assertThrows(InvalidAccessTokenException.class,
      () -> authorizationService.validateToken("INVALIDACCESSTOKEN"));

    // Then
    Assertions.assertEquals(
      "Bad Access Token provided",
      result.getMessage()
    );
  }


}
