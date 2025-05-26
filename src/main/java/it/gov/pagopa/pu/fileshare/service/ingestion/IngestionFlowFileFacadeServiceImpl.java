package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.IngestionFlowFileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.UnauthorizedFileDownloadException;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType.DP_INSTALLMENTS;

@Slf4j
@Service
public class IngestionFlowFileFacadeServiceImpl implements IngestionFlowFileFacadeService {
  private final UserAuthorizationService userAuthorizationService;
  private final FileService fileService;
  private final FileStorerService fileStorerService;
  private final FoldersPathsConfig foldersPathsConfig;
  private final IngestionFlowFileService ingestionFlowFileService;
  private final IngestionFlowFileDTOMapper ingestionFlowFileDTOMapper;
  private final String archivedSubFolder;
  private final String errorsSubFolder;
  private final DuplicateIngestionFlowFileRequestHandlerService duplicateIngestionFlowFileRequestHandlerService;

  public IngestionFlowFileFacadeServiceImpl(
    @Value("${folders.process-target-sub-folders.archive}") String archivedSubFolder,
    @Value("${folders.process-target-sub-folders.errors}") String errorsSubFolder,

    UserAuthorizationService userAuthorizationService,
    FileService fileService,
    FileStorerService fileStorerService,
    FoldersPathsConfig foldersPathsConfig,
    IngestionFlowFileService ingestionFlowFileService,
    IngestionFlowFileDTOMapper ingestionFlowFileDTOMapper,
    DuplicateIngestionFlowFileRequestHandlerService duplicateIngestionFlowFileRequestHandlerService
  ) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileService = fileService;
    this.fileStorerService = fileStorerService;
    this.foldersPathsConfig = foldersPathsConfig;
    this.ingestionFlowFileService = ingestionFlowFileService;
    this.ingestionFlowFileDTOMapper = ingestionFlowFileDTOMapper;
    this.archivedSubFolder = archivedSubFolder;
    this.errorsSubFolder = errorsSubFolder;
    this.duplicateIngestionFlowFileRequestHandlerService = duplicateIngestionFlowFileRequestHandlerService;
  }

  @Override
  public Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType,
                                      FileOrigin fileOrigin, String fileName, MultipartFile multipartFile,
                                      Long ingestionFlowFileId,
                                      UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    if (ingestionFlowFileId != null) {
      IngestionFlowFile ingestionFlowFile = ingestionFlowFileService.getIngestionFlowFile(ingestionFlowFileId, accessToken);
      if (ingestionFlowFile == null || ingestionFlowFile.getStatus() != IngestionFlowFileStatus.WAITING_FILE) {
        throw new IngestionFlowFileNotFoundException(
          "IngestionFlowFile with id %d and status WAITING_FILE not found".formatted(ingestionFlowFileId));
      }
    }

    fileService.validateFile(multipartFile);

    String ingestionFlowFilePath = foldersPathsConfig.getIngestionFlowFilePath(ingestionFlowFileType);

    if(fileStorerService.checkIfAlreadyUploadedOrArchived(organizationId, archivedSubFolder, ingestionFlowFilePath, fileName)) {
      duplicateIngestionFlowFileRequestHandlerService.handleDuplicateFile(organizationId, archivedSubFolder, ingestionFlowFilePath, fileName, accessToken);
    }

    String fileVersion = null;
    if (ingestionFlowFileType.equals(DP_INSTALLMENTS)) {
      List<String> fileVersions = ingestionFlowFileService.getIngestionFlowFileVersion(
        IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS, accessToken);

      fileVersion = fileService.validateVersionFromIngestionFlowFilename(fileVersions, fileName);
    }

    String filePath = fileStorerService.saveToSharedFolder(organizationId, multipartFile,
      ingestionFlowFilePath, fileName).getRelativePath();

    return ingestionFlowFileService.createIngestionFlowFile(
      ingestionFlowFileDTOMapper.mapToIngestionFlowFileDTO(ingestionFlowFileId, multipartFile,
        ingestionFlowFileType, fileOrigin, organizationId, filePath, fileVersion)
      , accessToken);
  }

  @Override
  public FileResourceDTO downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    IngestionFlowFile ingestionFlowFile = ingestionFlowFileService.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    if (ingestionFlowFile == null) {
      throw new FileNotFoundException("Ingestion flow file with id %s was not found".formatted(ingestionFlowFileId));
    }

    if(!organizationId.equals(ingestionFlowFile.getOrganizationId())){
      throw new AuthorizationDeniedException("Access Denied");
    }

    if (!AuthorizationService.isAdminRole(organizationId, user) &&
      !user.getMappedExternalUserId().equals(ingestionFlowFile.getOperatorExternalId())) {
      throw new UnauthorizedFileDownloadException(
        "User is not authorized to download ingestion flow file with ID " + ingestionFlowFileId);
    }

    Path filePath = getFilePath(ingestionFlowFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, ingestionFlowFile.getFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), ingestionFlowFile.getFileName());
  }

  @Override
  public FileResourceDTO downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    IngestionFlowFile ingestionFlowFile = ingestionFlowFileService.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    if (ingestionFlowFile == null) {
      throw new FileNotFoundException("Ingestion flow file with id %s was not found".formatted(ingestionFlowFileId));
    }

    if(!organizationId.equals(ingestionFlowFile.getOrganizationId())){
      throw new AuthorizationDeniedException("Access Denied");
    }

    if (!AuthorizationService.isAdminRole(organizationId, user) &&
      !user.getMappedExternalUserId().equals(ingestionFlowFile.getOperatorExternalId())) {
      throw new UnauthorizedFileDownloadException(
        "User is not authorized to download ingestion flow file with ID " + ingestionFlowFileId);
    }

    Path filePath = getErrorsFilePath(ingestionFlowFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, ingestionFlowFile.getDiscardFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), ingestionFlowFile.getDiscardFileName());
  }

  private Path getFilePath(IngestionFlowFile ingestionFlowFile) {
    return fileStorerService.getUploadedOrArchivedPath(ingestionFlowFile.getOrganizationId(), archivedSubFolder, ingestionFlowFile.getFilePathName(), ingestionFlowFile.getFileName())
      .getParent();
  }

  private Path getErrorsFilePath(IngestionFlowFile ingestionFlowFile) {
    if (ingestionFlowFile.getDiscardFileName() == null) {
      throw new FileNotFoundException("Ingestion flow file with id %s has no errors file".formatted(ingestionFlowFile.getIngestionFlowFileId()));
    }

    return fileStorerService.getUploadedOrArchivedPath(ingestionFlowFile.getOrganizationId(), errorsSubFolder, ingestionFlowFile.getFilePathName(), ingestionFlowFile.getDiscardFileName())
      .getParent();
  }
}
