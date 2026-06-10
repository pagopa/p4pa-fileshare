package it.gov.pagopa.pu.fileshare.mapper;

import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class IngestionFlowFileDTOMapper {
  public IngestionFlowFileRequestDTO mapToIngestionFlowFileDTO(
    Long ingestionFlowFileId, MultipartFile multipartFile, IngestionFlowFileType ingestionFlowFileType, FileOrigin fileOrigin, Long organizationId, String filePath, String fileName, String fileVersion) {

    IngestionFlowFileRequestDTO ingestionFlowFileRequestDTO = new IngestionFlowFileRequestDTO();
    ingestionFlowFileRequestDTO.setIngestionFlowFileId(ingestionFlowFileId);
    ingestionFlowFileRequestDTO.setOrganizationId(organizationId);
    ingestionFlowFileRequestDTO.setFilePathName(filePath);
    ingestionFlowFileRequestDTO.setFileName(fileName);
    ingestionFlowFileRequestDTO.setFileSize(multipartFile.getSize());
    ingestionFlowFileRequestDTO.setIngestionFlowFileType(IngestionFlowFileTypeEnum.valueOf(ingestionFlowFileType.toString()));
    ingestionFlowFileRequestDTO.fileOrigin(fileOrigin.toString());
    ingestionFlowFileRequestDTO.fileVersion(fileVersion);

    return ingestionFlowFileRequestDTO;
  }

}
