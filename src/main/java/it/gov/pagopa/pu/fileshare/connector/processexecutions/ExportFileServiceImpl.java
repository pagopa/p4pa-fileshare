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
  public ExportFile getExportFile(Long exportFileId, String accessToken) {
    return client.getExportFile(exportFileId, accessToken);
  }
}
