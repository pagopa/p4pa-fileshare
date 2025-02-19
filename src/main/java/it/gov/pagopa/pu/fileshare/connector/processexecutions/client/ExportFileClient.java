package it.gov.pagopa.pu.fileshare.connector.processexecutions.client;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.config.ProcessExecutionsApisHolder;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class ExportFileClient {
  private final ProcessExecutionsApisHolder processExecutionsApisHolder;

  public ExportFileClient(
    ProcessExecutionsApisHolder processExecutionsApisHolder) {
    this.processExecutionsApisHolder = processExecutionsApisHolder;
  }

  public ExportFile getExportFile(Long exportFileId, String accessToken) {
    try {
      log.debug("Fetching export file with ID [{}]", exportFileId);
      return processExecutionsApisHolder.getExportFileEntityControllerApi(accessToken).crudGetExportfile(String.valueOf(exportFileId));
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Cannot find ExportFile with ID [{}]", exportFileId, e);
      return null;
    }
  }

}
