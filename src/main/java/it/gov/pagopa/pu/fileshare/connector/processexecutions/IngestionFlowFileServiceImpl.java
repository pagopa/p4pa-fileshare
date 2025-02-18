package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class IngestionFlowFileServiceImpl implements IngestionFlowFileService {

  private final IngestionFlowFileClient client;

  public IngestionFlowFileServiceImpl(IngestionFlowFileClient client) {
    this.client = client;
  }

  @Override
  public Long createIngestionFlowFile(IngestionFlowFileRequestDTO ingestionFlowFileDTO, String accessToken) {
    return client.createIngestionFlowFile(ingestionFlowFileDTO, accessToken);
  }

  @Override
  public IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken) {
    return client.getIngestionFlowFile(ingestionFlowFileId, accessToken);
  }
}
