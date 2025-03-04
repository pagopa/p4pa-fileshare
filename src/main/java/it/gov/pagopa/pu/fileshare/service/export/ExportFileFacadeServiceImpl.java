package it.gov.pagopa.pu.fileshare.service.export;


import it.gov.pagopa.pu.fileshare.connector.processexecutions.ExportFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.ExportFile;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

@Service
public class ExportFileFacadeServiceImpl implements ExportFileFacadeService {
  private final UserAuthorizationService userAuthorizationService;
  private final FileStorerService fileStorerService;
  private final ExportFileService exportFileService;

  public ExportFileFacadeServiceImpl(
    UserAuthorizationService userAuthorizationService,
    FileStorerService fileStorerService,
    ExportFileService exportFileService) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileStorerService = fileStorerService;
    this.exportFileService = exportFileService;
  }

  @Override
  public FileResourceDTO downloadExportFile(Long organizationId,
    Long exportFileId, UserInfo user,
    String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user,
      accessToken);

    ExportFile exportFile = exportFileService.getExportFile(exportFileId,
      accessToken);

    if (exportFile == null) {
      throw new FileNotFoundException(
        "Export file with id %s was not found".formatted(exportFileId));
    }

    if (!AuthorizationService.isAdminRole(organizationId, user) &&
      !user.getMappedExternalUserId()
        .equals(exportFile.getOperatorExternalId())) {
      throw new UnauthorizedFileDownloadException(
        "User is not authorized to download export file with ID "
          + exportFileId);
    }

    Path filePath = getFilePath(exportFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath,
      exportFile.getFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream),
      exportFile.getFileName());
  }

  private Path getFilePath(ExportFile exportFile) {
    if(StringUtils.isEmpty(exportFile.getFilePathName())){
      throw new FileNotFoundException("ExportFile not ready");
    }
    Path organizationBasePath = fileStorerService.buildOrganizationBasePath(
      exportFile.getOrganizationId());

    return organizationBasePath
      .resolve(exportFile.getFilePathName());
  }
}
