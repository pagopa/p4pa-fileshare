package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.auth.client.AuthnClient;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthorizationService {

  private final AuthnClient authnClient;

  public AuthorizationService(AuthnClient authnClient) {
    this.authnClient = authnClient;
  }

  public UserInfo validateToken(String accessToken){
    log.info("Requesting validate token");
    return authnClient.getUserInfo(accessToken);
  }
}
