package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.springframework.web.multipart.MultipartFile;

public interface IngestionFlowFileService {
  String uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType,
                                 FileOrigin fileOrigin, MultipartFile ingestionFlowFile, UserInfo user, String accessToken);

  FileResourceDTO downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken);

}
