package it.gov.pagopa.pu.fileshare.connector.processexecutions;

import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;

import java.util.List;

public interface IngestionFlowFileService {
  Long createIngestionFlowFile(IngestionFlowFileRequestDTO ingestionFlowFileDTO, String accessToken);
  IngestionFlowFile getIngestionFlowFile(Long ingestionFlowFileId, String accessToken);
  IngestionFlowFile findByOrganizationIdAndFilePathNameAndFileName(Long organizationId, String filePathName, String fileName, String accessToken);
  Integer updateFileNames(Long ingestionFlowFileId, String fileName, String discardFileName, String accessToken);
  List<String> getIngestionFlowFileVersion(IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum ingestionFlowFileType, String accessToken);
}
