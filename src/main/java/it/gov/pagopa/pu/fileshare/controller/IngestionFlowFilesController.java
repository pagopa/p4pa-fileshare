package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IngestionFlowFilesController implements IngestionFlowFileApi {

  private final IngestionFlowFileService ingestionFlowFileService;

  public IngestionFlowFilesController(
    IngestionFlowFileService ingestionFlowFileService) {
    this.ingestionFlowFileService = ingestionFlowFileService;
  }

  @Override
  public ResponseEntity<UploadIngestionFlowFileResponseDTO> uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, FileOrigin fileOrigin, MultipartFile ingestionFlowFile) {
    String ingestionFlowFileId = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, fileOrigin, ingestionFlowFile, SecurityUtils.getLoggedUser(),
      SecurityUtils.getAccessToken());
    return ResponseEntity.ok(new UploadIngestionFlowFileResponseDTO(ingestionFlowFileId));
  }
}
