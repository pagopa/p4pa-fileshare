package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.PagedModelIngestionFlowFileEmbedded;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IngestionFlowFileSearchClient {

  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public IngestionFlowFileSearchClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public List<IngestionFlowFile> findByOrganizationIdAndFilePathNameAndFileName(Long organizationId, String filePathName, String fileName, String accessToken) {
    PagedModelIngestionFlowFileEmbedded embedded = processExecutionsApisHolder.getIngestionFlowFileSearchControllerApi(accessToken)
      .crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName)
      .getEmbedded();
    return embedded != null
      ? embedded.getIngestionFlowFiles()
      : List.of();
  }

}
