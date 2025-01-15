package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class IngestionFlowFileClient {
  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public IngestionFlowFileClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public IngestionFlowFileDTO createIngestionFlowFile(IngestionFlowFileDTO ingestionFlowFileDTO, String accessToken) {
    try{
      return processExecutionsApisHolder.getIngestionFlowFileControllerApi(accessToken).createIngestionFlowFile(ingestionFlowFileDTO);
    } catch (HttpClientErrorException e) {
      log.error("Error creating ingestion flow file", e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error creating ingestion flow file", e);
      throw e;
    }
  }
}
