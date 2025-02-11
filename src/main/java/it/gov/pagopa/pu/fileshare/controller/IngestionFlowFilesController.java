package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.dto.generated.UploadIngestionFlowFileResponseDTO;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
  public ResponseEntity<UploadIngestionFlowFileResponseDTO> uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, FileOrigin fileOrigin, String fileName, MultipartFile ingestionFlowFile) {
    Long ingestionFlowFileId = ingestionFlowFileService.uploadIngestionFlowFile(organizationId, ingestionFlowFileType, fileOrigin, fileName, ingestionFlowFile, SecurityUtils.getLoggedUser(),
      SecurityUtils.getAccessToken());
    return ResponseEntity.ok(new UploadIngestionFlowFileResponseDTO(ingestionFlowFileId));
  }


  @Override
  public ResponseEntity<Resource> downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId) {
    FileResourceDTO fileResourceDTO = ingestionFlowFileService.downloadIngestionFlowFile(organizationId, ingestionFlowFileId, SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());

    Resource fileResource = new InputStreamResource(fileResourceDTO.getResourceStream());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment()
      .filename(fileResourceDTO.getFileName())
      .build());

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .headers(headers)
      .body(fileResource);
  }

}
