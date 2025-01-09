package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.connector.organization.client.OrganizationClient;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paorganization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class IngestionFlowFileServiceImpl implements IngestionFlowFileService {
  private final OrganizationClient organizationClient;

  public IngestionFlowFileServiceImpl(OrganizationClient organizationClient) {
    this.organizationClient = organizationClient;
  }

  @Override
  public void uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, MultipartFile ingestionFlowFile, UserInfo user, String accessToken) {
    checkUserAuthorization(organizationId, user, accessToken);

  }

  private void checkUserAuthorization(Long organizationId, UserInfo user, String accessToken) {
    Organization organization = organizationClient.getOrganizationById(organizationId, accessToken);
    boolean isAuthorized = user.getOrganizations().stream()
      .anyMatch(o -> o.getOrganizationIpaCode().equals(organization.getIpaCode())
        && !CollectionUtils.isEmpty(o.getRoles()));
    if(!isAuthorized){
      log.debug("Unauthorized user. [organizationId:{}]", organizationId);
      throw new AuthorizationDeniedException("Access Denied");
    }
  }
}
