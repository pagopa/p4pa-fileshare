package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile;

public interface ExportFileService {
  ExportFile getExportFile(Long exportFileId, String accessToken);
}
