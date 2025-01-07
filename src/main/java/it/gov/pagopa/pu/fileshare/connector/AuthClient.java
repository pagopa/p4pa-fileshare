package it.gov.pagopa.pu.fileshare.connector;

import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;

public interface AuthClient {

  UserInfo validateToken(String accessToken);

}
