package it.gov.pagopa.pu.fileshare.mapper;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO.FlowFileTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class IngestionFlowFileDTOMapper {
  public IngestionFlowFileRequestDTO mapToIngestionFlowFileDTO(
    MultipartFile ingestionFlowFile, IngestionFlowFileType ingestionFlowFileType, FileOrigin fileOrigin, Long organizationId, String filePath) {

    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    ingestionFlowFileRequestDTO.setOrganizationId(organizationId);
    ingestionFlowFileRequestDTO.setFilePathName(filePath);
    ingestionFlowFileRequestDTO.setFileName(StringUtils.defaultString(ingestionFlowFile.getOriginalFilename()));
    ingestionFlowFileRequestDTO.setFileSize(ingestionFlowFile.getSize());
    ingestionFlowFileRequestDTO.flowFileType(FlowFileTypeEnum.valueOf(ingestionFlowFileType.toString()));
    ingestionFlowFileRequestDTO.fileOrigin(fileOrigin.toString());

    return ingestionFlowFileRequestDTO;
  }

}
