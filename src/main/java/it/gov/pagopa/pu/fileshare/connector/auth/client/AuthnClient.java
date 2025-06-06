package it.gov.pagopa.pu.fileshare.connector.auth.client;

import it.gov.pagopa.pu.fileshare.connector.auth.config.AuthApisHolder;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class AuthnClient {

  private final AuthApisHolder authApisHolder;

  public AuthnClient(AuthApisHolder authApisHolder) {
    this.authApisHolder = authApisHolder;
  }

  public UserInfo getUserInfo(String accessToken) {
    try {
      return authApisHolder.getAuthnApi(accessToken)
        .getUserInfo();
    } catch (HttpClientErrorException.Unauthorized e) {
      throw new InvalidAccessTokenException(e.getResponseBodyAsString());
    }
  }

}
