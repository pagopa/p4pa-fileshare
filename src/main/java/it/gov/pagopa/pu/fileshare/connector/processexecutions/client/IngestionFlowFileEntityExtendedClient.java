package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IngestionFlowFileEntityExtendedClient {

  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public IngestionFlowFileEntityExtendedClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public Integer updateFileNames(Long ingestionFlowFileId, String fileName, String discardFileName, String accessToken) {
    return processExecutionsApisHolder.getIngestionFlowFileEntityExtendedControllerApi(accessToken)
      .updateFileNames(ingestionFlowFileId, fileName, discardFileName);
  }

}
