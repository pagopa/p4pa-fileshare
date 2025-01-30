package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class IngestionFlowFileClient {
  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public IngestionFlowFileClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public String createIngestionFlowFile(IngestionFlowFileRequestDTO ingestionFlowFileDTO, String accessToken) {
    try {
      return processExecutionsApisHolder.getIngestionFlowFileControllerApi(accessToken)
        .createIngestionFlowFileWithHttpInfo(ingestionFlowFileDTO)
        .getHeaders().getFirst(HttpHeaders.LOCATION);
    } catch (HttpClientErrorException e) {
      log.error("Error creating ingestion flow file", e);
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error creating ingestion flow file", e);
      throw e;
    }
  }

  public IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken) {
    try {
      log.debug("Fetching ingestion flow file with ID [{}]", ingestionFlowFileId);
      return processExecutionsApisHolder.getIngestionFlowFileEntityControllerApi(accessToken).crudGetIngestionflowfile(String.valueOf(ingestionFlowFileId));
    } catch (HttpClientErrorException e) {
      log.error("Error fetching ingestion flow file with ID [{}]", ingestionFlowFileId, e);
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingestion Flow File not found");
      }
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error fetching ingestion flow file with ID [{}]", ingestionFlowFileId, e);
      throw e;
    }
  }

}
