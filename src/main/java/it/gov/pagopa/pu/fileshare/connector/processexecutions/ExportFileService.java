package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;

public interface ExportFileService {
  ExportFile getExportFile(Long exportFileId, String accessToken);
}
