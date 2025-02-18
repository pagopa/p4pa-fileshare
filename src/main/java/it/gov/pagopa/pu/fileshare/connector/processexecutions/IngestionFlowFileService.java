package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;

public interface IngestionFlowFileService {
  Long createIngestionFlowFile(IngestionFlowFileRequestDTO ingestionFlowFileDTO, String accessToken);
  IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken);
}
