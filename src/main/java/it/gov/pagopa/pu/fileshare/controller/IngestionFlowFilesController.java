package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileFacadeService;
import it.gov.pagopa.pu.fileshare.util.Utilities;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IngestionFlowFilesController implements IngestionFlowFileApi {

  private final IngestionFlowFileFacadeService ingestionFlowFileFacadeService;

  public IngestionFlowFilesController(IngestionFlowFileFacadeService ingestionFlowFileFacadeService) {
    this.ingestionFlowFileFacadeService = ingestionFlowFileFacadeService;
  }

  @Override
  public ResponseEntity<UploadIngestionFlowFileResponseDTO> uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, FileOrigin fileOrigin, MultipartFile ingestionFlowFile, String fileName, Long ingestionFlowFileId) {
    Long retrievedIngestionFlowFileId = ingestionFlowFileFacadeService.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, fileOrigin, fileName, ingestionFlowFile, ingestionFlowFileId, SecurityUtils.getLoggedUser(),
      SecurityUtils.getAccessToken());
    return ResponseEntity.ok(new UploadIngestionFlowFileResponseDTO(retrievedIngestionFlowFileId));
  }

  @Override
  public ResponseEntity<Resource> downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId) {
    FileResourceDTO fileResourceDTO = ingestionFlowFileFacadeService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());

    return Utilities.buildResourceResponseEntity(fileResourceDTO);
  }

  @Override
  public ResponseEntity<Resource> downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId) {
    FileResourceDTO fileResourceDTO = ingestionFlowFileFacadeService.downloadIngestionFlowErrorsFile(
      organizationId, ingestionFlowFileId, SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());

    return Utilities.buildResourceResponseEntity(fileResourceDTO);
  }

  @Override
  public ResponseEntity<Resource> downloadNotice(Long organizationId, Long ingestionFlowFileId) {
    FileResourceDTO fileResourceDTO = ingestionFlowFileFacadeService
      .downloadNotice(organizationId, ingestionFlowFileId, SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());

    return Utilities.buildResourceResponseEntity(fileResourceDTO);
  }

  @Override
  public ResponseEntity<Resource> downloadIuvFile(Long organizationId, Long ingestionFlowFileId) {
    FileResourceDTO fileResourceDTO = ingestionFlowFileFacadeService
      .downloadIuvFile(organizationId, ingestionFlowFileId, SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());

    return Utilities.buildResourceResponseEntity(fileResourceDTO);
  }
}
