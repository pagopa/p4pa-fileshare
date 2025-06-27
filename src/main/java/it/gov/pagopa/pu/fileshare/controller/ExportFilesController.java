package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.ExportFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.export.ExportFileFacadeService;
import it.gov.pagopa.pu.fileshare.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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

    FileResourceDTO fileResourceDTO = exportFileFacadeService.downloadExportFile(
      organizationId, exportFileId, SecurityUtils.getLoggedUser(),
      SecurityUtils.getAccessToken());

    return Utilities.buildResourceResponseEntity(fileResourceDTO);
  }

}
