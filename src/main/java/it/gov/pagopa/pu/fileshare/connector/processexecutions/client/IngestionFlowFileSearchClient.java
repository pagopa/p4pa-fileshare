package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class IngestionFlowFileSearchClient {

  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public IngestionFlowFileSearchClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public IngestionFlowFile findByOrganizationIdAndFilePathNameAndFileName(Long organizationId, String filePathName, String fileName, String accessToken) {
    try {
      return processExecutionsApisHolder.getIngestionFlowFileSearchControllerApi(accessToken)
        .crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName);
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Cannot find IngestionFlowFile related to organizationId {} having filePathName= {}, fileName={}", organizationId, filePathName, fileName);
      return null;
    }
  }

}
