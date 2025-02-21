package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.ExportFileClient;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import org.springframework.stereotype.Service;

@Service
public class ExportFileServiceImpl implements ExportFileService {

  private final ExportFileClient client;

  public ExportFileServiceImpl(ExportFileClient client) {
    this.client = client;
  }

  @Override
  public ExportFile getExportFile(Long exportFileId, Long organizationId, UserInfo loggedUser, String accessToken) {
    boolean isLoggedUserAdmin = AuthorizationService.isAdminRole(
      organizationId, loggedUser);
    String operatorExternalUserId = null;

    if(!isLoggedUserAdmin){
      operatorExternalUserId = loggedUser.getMappedExternalUserId();
    }

    ExportFile exportFile = client.getExportFile(exportFileId, accessToken);

    if (!isLoggedUserAdmin && !operatorExternalUserId.equals(exportFile.getOperatorExternalId())) {
      throw new UnauthorizedFileDownloadException("User is not authorized to download export file with ID " + exportFileId);
    }

    return exportFile;
  }
}
