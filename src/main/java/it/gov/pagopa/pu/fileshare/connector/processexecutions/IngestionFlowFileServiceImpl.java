package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileEntityClient;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileEntityExtendedClient;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileSearchClient;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFileRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionFlowFileServiceImpl implements IngestionFlowFileService {

  private final IngestionFlowFileEntityClient entityClient;
  private final IngestionFlowFileEntityExtendedClient entityExtendedClient;
  private final IngestionFlowFileSearchClient searchClient;

  public IngestionFlowFileServiceImpl(IngestionFlowFileEntityClient entityClient, IngestionFlowFileEntityExtendedClient entityExtendedClient, IngestionFlowFileSearchClient searchClient) {
    this.entityClient = entityClient;
    this.entityExtendedClient = entityExtendedClient;
    this.searchClient = searchClient;
  }

  @Override
  public Long createIngestionFlowFile(IngestionFlowFileRequestDTO ingestionFlowFileDTO, String accessToken) {
    return entityClient.createIngestionFlowFile(ingestionFlowFileDTO, accessToken);
  }

  @Override
  public IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken) {
    return entityClient.getIngestionFlowFile(ingestionFlowFileId, accessToken);
  }

  @Override
  public List<IngestionFlowFile> findByOrganizationIdAndFilePathNameAndFileName(Long organizationId, String filePathName, String fileName, String accessToken) {
    return searchClient.findByOrganizationIdAndFilePathNameAndFileName(organizationId, filePathName, fileName, accessToken);
  }

  @Override
  public Integer updateFileNames(Long ingestionFlowFileId, String fileName, String discardFileName, String accessToken) {
    return entityExtendedClient.updateFileNames(ingestionFlowFileId, fileName, discardFileName, accessToken);
  }

  @Override
  public List<String> getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum ingestionFlowFileType, String accessToken) {
    return entityClient.getIngestionFlowFileVersion(ingestionFlowFileType, accessToken);
  }
}
