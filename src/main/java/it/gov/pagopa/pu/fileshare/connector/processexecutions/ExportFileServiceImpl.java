package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.ExportFileEntityClient;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import org.springframework.stereotype.Service;

@Service
public class ExportFileServiceImpl implements ExportFileService {

  private final ExportFileEntityClient client;

  public ExportFileServiceImpl(ExportFileEntityClient client) {
    this.client = client;
  }

  @Override
  public ExportFile getExportFile(Long exportFileId, String accessToken) {
    return client.getExportFile(exportFileId, accessToken);
  }
}
