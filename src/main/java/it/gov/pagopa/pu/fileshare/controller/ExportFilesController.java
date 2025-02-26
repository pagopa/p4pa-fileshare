package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.ExportFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.export.ExportFileFacadeService;
import java.io.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ExportFilesController implements ExportFileApi {

  private final ExportFileFacadeService exportFileFacadeService;

  public ExportFilesController(
    ExportFileFacadeService exportFileFacadeService) {
    this.exportFileFacadeService = exportFileFacadeService;
  }


  @Override
  public ResponseEntity<Resource> downloadExportFile(Long organizationId,
    Long exportFileId) {
    log.info(
      "Requesting to download export file [exportFileId: {}] of organization [organizationId: {}]",
      exportFileId, organizationId);

    FileResourceDTO fileResourceDTO;
    try {
      fileResourceDTO = exportFileFacadeService.downloadExportFile(
        organizationId, exportFileId, SecurityUtils.getLoggedUser(),
        SecurityUtils.getAccessToken());
    } catch (FileNotFoundException e) {
      return ResponseEntity.notFound().build();
    }

    Resource fileResource = new InputStreamResource(
      fileResourceDTO.getResourceStream());

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
