package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
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
  public ResponseEntity<Void> uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, MultipartFile ingestionFlowFile) {
    ingestionFlowFileService.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, ingestionFlowFile, SecurityUtils.getLoggedUser(),
      SecurityUtils.getAccessToken());
    return ResponseEntity.ok(null);
  }
}
