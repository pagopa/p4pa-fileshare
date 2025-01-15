package it.gov.pagopa.pu.fileshare.mapper;

import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileDTO.StatusEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class IngestionFlowFileDTOMapper {
  public IngestionFlowFileDTO mapToIngestionFlowFileDTO(
    MultipartFile ingestionFlowFile, Long organizationId, String filePath) {
    return IngestionFlowFileDTO.builder()
      .organizationId(organizationId)
      .status(StatusEnum.UPLOADED)
      .filePath(filePath)
      .fileName(ingestionFlowFile.getOriginalFilename())
      .fileSize(ingestionFlowFile.getSize())
      .build();
  }
}
