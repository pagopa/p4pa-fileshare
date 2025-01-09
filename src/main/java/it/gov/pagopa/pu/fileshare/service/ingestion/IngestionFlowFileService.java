package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.springframework.web.multipart.MultipartFile;

public interface IngestionFlowFileService {
  void uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, MultipartFile ingestionFlowFile, UserInfo user, String accessToken);
}
