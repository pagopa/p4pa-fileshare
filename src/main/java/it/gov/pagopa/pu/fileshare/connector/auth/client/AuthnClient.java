package it.gov.pagopa.pu.fileshare.connector.auth.client;

import it.gov.pagopa.pu.fileshare.connector.auth.config.AuthApisHolder;
import it.gov.pagopa.pu.fileshare.exception.common.RestInvokeNotAuthorizedException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidAccessTokenException;
import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import org.springframework.stereotype.Service;

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
    } catch (RestInvokeNotAuthorizedException e) {
      throw new InvalidAccessTokenException("INVALID_ACCESS_TOKEN", e.getMessage());
    }
  }

}
