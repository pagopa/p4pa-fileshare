package it.gov.pagopa.pu.fileshare.service;

import it.gov.pagopa.pu.fileshare.connector.AuthClient;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthorizationService {

  private final AuthClient authClient;

  public AuthorizationService(AuthClient authClient) {
    this.authClient = authClient;
  }

  public UserInfo validateToken(String accessToken){
    log.info("Requesting validate token");
    return authClient.validateToken(accessToken);
  }
}
