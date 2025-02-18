package it.gov.pagopa.pu.fileshare.service.export;


import static it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile.StatusEnum.COMPLETED;
import static it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile.StatusEnum.ERROR;

import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.ExportFileClient;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import java.io.InputStream;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;

public class ExportFileFacadeServiceImpl implements ExportFileFacadeService {
  private final UserAuthorizationService userAuthorizationService;
  private final FileStorerService fileStorerService;
  private final ExportFileClient exportFileClient;
  private final String archivedSubFolder;

  public ExportFileFacadeServiceImpl(
    UserAuthorizationService userAuthorizationService,
    FileStorerService fileStorerService,
    ExportFileClient exportFileClient,
    @Value("${folders.process-target-sub-folders.archive}") String archivedSubFolder) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileStorerService = fileStorerService;
    this.exportFileClient = exportFileClient;
    this.archivedSubFolder = archivedSubFolder;
  }

  @Override
  public FileResourceDTO downloadExportFile(Long organizationId,
    Long exportFileId, UserInfo user,
    String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    ExportFile exportFile = exportFileClient.getExportFile(exportFileId, accessToken);

    Path filePath = getFilePath(exportFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, exportFile.getFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), exportFile.getFileName());
  }

  private Path getFilePath(ExportFile exportFile) {
    Path organizationBasePath = fileStorerService.buildOrganizationBasePath(exportFile.getOrganizationId());

    Path filePath = organizationBasePath
      .resolve(exportFile.getFilePathName());
    if (exportFile.getStatus() == COMPLETED || exportFile.getStatus() == ERROR) {
      filePath = filePath
        .resolve(archivedSubFolder);
    }
    return filePath;
  }
}
