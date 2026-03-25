package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.IngestionFlowFileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileTypeException;
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
import java.util.Map;
import java.util.Objects;

import static it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType.*;
import static java.util.Map.entry;

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

  private static final Map<IngestionFlowFileType, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum> fileTypeMapping = Map.ofEntries(
    entry(DP_INSTALLMENTS, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DP_INSTALLMENTS),
    entry(RECEIPT, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.RECEIPT),
    entry(TREASURY_OPI, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.TREASURY_OPI),
    entry(TREASURY_POSTE, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.TREASURY_POSTE),
    entry(TREASURY_CSV, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.TREASURY_CSV),
    entry(TREASURY_CSV_COMPLETE, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.TREASURY_CSV_COMPLETE),
    entry(TREASURY_XLS, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.TREASURY_XLS),
    entry(PAYMENT_NOTIFICATION, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.PAYMENT_NOTIFICATION),
    entry(ORGANIZATIONS, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.ORGANIZATIONS),
    entry(DEBT_POSITIONS_TYPE, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE),
    entry(DEBT_POSITIONS_TYPE_ORG, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.DEBT_POSITIONS_TYPE_ORG),
    entry(SEND_NOTIFICATION, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.SEND_NOTIFICATION),
    entry(ASSESSMENTS_REGISTRY, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.ASSESSMENTS_REGISTRY),
    entry(ASSESSMENTS, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.ASSESSMENTS),
    entry(ORGANIZATIONS_SIL_SERVICE, IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum.ORGANIZATIONS_SIL_SERVICE)
  );

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
                                      FileOrigin fileOrigin, String overridingFileName, MultipartFile multipartFile,
                                      Long ingestionFlowFileId,
                                      UserInfo user, String accessToken) {
    String fileName = Objects.requireNonNullElse(overridingFileName, multipartFile.getOriginalFilename());

    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    if (ingestionFlowFileId != null) {
      IngestionFlowFile ingestionFlowFile = ingestionFlowFileService.getIngestionFlowFile(ingestionFlowFileId, accessToken);
      if (ingestionFlowFile == null ||
        !IngestionFlowFileStatus.WAITING_FILE.equals(ingestionFlowFile.getStatus()) ||
        !ingestionFlowFile.getFileOrigin().equals(String.valueOf(fileOrigin))) {
        throw new IngestionFlowFileNotFoundException(
          "FILE_NOT_FOUND", "IngestionFlowFile in WAITING_FILE status and matching origin not found with id %d%s"
            .formatted(ingestionFlowFileId, ingestionFlowFile == null ? "" : " - actual status: " + ingestionFlowFile.getStatus() + ", origin: " + ingestionFlowFile.getFileOrigin()));
      }
    }

    fileService.validateFile(multipartFile);

    String ingestionFlowFilePath = foldersPathsConfig.getIngestionFlowFilePath(ingestionFlowFileType);

    if (fileStorerService.checkIfAlreadyUploadedOrArchived(organizationId, archivedSubFolder, ingestionFlowFilePath, fileName)) {
      duplicateIngestionFlowFileRequestHandlerService.handleDuplicateFile(organizationId, archivedSubFolder, ingestionFlowFilePath, fileName, accessToken);
    }

    String fileVersion = getFileVersion(ingestionFlowFileType, fileName, accessToken);
    String filePath = fileStorerService.saveToSharedFolder(organizationId, multipartFile,
      ingestionFlowFilePath, fileName).getRelativePath();

    return ingestionFlowFileService.createIngestionFlowFile(
      ingestionFlowFileDTOMapper.mapToIngestionFlowFileDTO(ingestionFlowFileId, multipartFile,
        ingestionFlowFileType, fileOrigin, organizationId, filePath, fileVersion)
      , accessToken);
  }

  @Override
  public FileResourceDTO downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    IngestionFlowFile ingestionFlowFile = authorizeDownload(organizationId, ingestionFlowFileId, user, accessToken);

    Path filePath = getFilePath(ingestionFlowFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, ingestionFlowFile.getFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), ingestionFlowFile.getFileName());
  }

  @Override
  public FileResourceDTO downloadIngestionFlowErrorsFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    IngestionFlowFile ingestionFlowFile = authorizeDownload(organizationId, ingestionFlowFileId, user, accessToken);

    Path filePath = getErrorsFilePath(ingestionFlowFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, ingestionFlowFile.getDiscardFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), ingestionFlowFile.getDiscardFileName());
  }

  @Override
  public FileResourceDTO downloadNotice(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    IngestionFlowFile ingestionFlowFile = authorizeDownload(organizationId, ingestionFlowFileId, user, accessToken);

    return downloadFileWithSuffix(ingestionFlowFile, "_notice.zip");
  }

  @Override
  public FileResourceDTO downloadIuvFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    IngestionFlowFile ingestionFlowFile = authorizeDownload(organizationId, ingestionFlowFileId, user, accessToken);

    if (!IngestionFlowFile.IngestionFlowFileTypeEnum.DP_INSTALLMENTS.equals(ingestionFlowFile.getIngestionFlowFileType())) {
      throw new InvalidFileTypeException("INVALID_FILE_TYPE", String.format("It's not possible to download IUV file for ingestionFlowFileId: %s. Expected type: %s, found: %s",
        ingestionFlowFileId,
        DP_INSTALLMENTS,
        ingestionFlowFile.getIngestionFlowFileType()));
    }

    return downloadFileWithSuffix(ingestionFlowFile, "_iuv.zip");
  }

  private FileResourceDTO downloadFileWithSuffix(IngestionFlowFile ingestionFlowFile, String suffix) {
    String newFileName = ingestionFlowFile.getFileName().replace(".zip", suffix);

    Path filePath = fileStorerService.getUploadedOrArchivedPath(
      ingestionFlowFile.getOrganizationId(), archivedSubFolder, ingestionFlowFile.getFilePathName(), newFileName).getParent();

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, newFileName);

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), newFileName);
  }

  private IngestionFlowFile authorizeDownload(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    IngestionFlowFile ingestionFlowFile = ingestionFlowFileService.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    if (ingestionFlowFile == null) {
      throw new FileNotFoundException("FILE_NOT_FOUND", "Ingestion flow file with id %s was not found".formatted(ingestionFlowFileId));
    }

    if (!organizationId.equals(ingestionFlowFile.getOrganizationId())) {
      throw new AuthorizationDeniedException("[USER_UNAUTHORIZED] Access Denied");
    }

    if (!AuthorizationService.isAdminRole(organizationId, user) &&
      !user.getMappedExternalUserId().equals(ingestionFlowFile.getOperatorExternalId())) {
      throw new UnauthorizedFileDownloadException(
        "USER_UNAUTHORIZED", "User is not authorized to download ingestion flow file with ID " + ingestionFlowFileId);
    }
    return ingestionFlowFile;
  }

  private Path getFilePath(IngestionFlowFile ingestionFlowFile) {
    return fileStorerService.getUploadedOrArchivedPath(ingestionFlowFile.getOrganizationId(), archivedSubFolder, ingestionFlowFile.getFilePathName(), ingestionFlowFile.getFileName()).getParent();
  }

  private Path getErrorsFilePath(IngestionFlowFile ingestionFlowFile) {
    if (ingestionFlowFile.getDiscardFileName() == null) {
      throw new FileNotFoundException("FILE_NOT_FOUND", "Ingestion flow file with id %s has no errors file".formatted(ingestionFlowFile.getIngestionFlowFileId()));
    }

    return fileStorerService.getUploadedOrArchivedPath(ingestionFlowFile.getOrganizationId(), errorsSubFolder, ingestionFlowFile.getFilePathName(), ingestionFlowFile.getDiscardFileName())
      .getParent();
  }

  private String getFileVersion(IngestionFlowFileType ingestionFlowFileType, String fileName, String accessToken) {
    IngestionFlowFileRequestDTO.IngestionFlowFileTypeEnum mappedType = fileTypeMapping.get(ingestionFlowFileType);

    if (mappedType == null) {
      return null;
    }

    List<String> fileVersions = ingestionFlowFileService.getIngestionFlowFileVersion(mappedType, accessToken);
    return fileService.validateVersionFromIngestionFlowFilename(fileVersions, fileName, mappedType);
  }
}
