package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileDecryptionException;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

import static it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile.StatusEnum.COMPLETED;
import static it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile.StatusEnum.ERROR;

@Slf4j
@Service
public class IngestionFlowFileServiceImpl implements IngestionFlowFileService {
  private final UserAuthorizationService userAuthorizationService;
  private final FileService fileService;
  private final FileStorerService fileStorerService;
  private final FoldersPathsConfig foldersPathsConfig;
  private final IngestionFlowFileClient ingestionFlowFileClient;
  private final IngestionFlowFileDTOMapper ingestionFlowFileDTOMapper;
  private final String validIngestionFlowFileExt;
  private final String archivedSubFolder;

  public IngestionFlowFileServiceImpl(
    UserAuthorizationService userAuthorizationService,
    FileService fileService,
    FileStorerService fileStorerService,
    FoldersPathsConfig foldersPathsConfig,
    IngestionFlowFileClient ingestionFlowFileClient,
    IngestionFlowFileDTOMapper ingestionFlowFileDTOMapper,
    @Value("${uploads.ingestion-flow-file.valid-extension}") String validIngestionFlowFileExt,
    @Value("${folders.process-target-sub-folders.archive}") String archivedSubFolder) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileService = fileService;
    this.fileStorerService = fileStorerService;
    this.foldersPathsConfig = foldersPathsConfig;
    this.ingestionFlowFileClient = ingestionFlowFileClient;
    this.ingestionFlowFileDTOMapper = ingestionFlowFileDTOMapper;
    this.validIngestionFlowFileExt = validIngestionFlowFileExt;
    this.archivedSubFolder = archivedSubFolder;
  }

  @Override
  public String uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType,
                                        FileOrigin fileOrigin, MultipartFile ingestionFlowFile, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    fileService.validateFile(ingestionFlowFile, validIngestionFlowFileExt);
    String filePath = fileStorerService.saveToSharedFolder(organizationId, ingestionFlowFile,
      foldersPathsConfig.getIngestionFlowFilePath(ingestionFlowFileType));

    return ingestionFlowFileClient.createIngestionFlowFile(
      ingestionFlowFileDTOMapper.mapToIngestionFlowFileDTO(ingestionFlowFile,
        ingestionFlowFileType, fileOrigin, organizationId, filePath)
      , accessToken);
  }

  @Override
  public FileResourceDTO downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    IngestionFlowFile ingestionFlowFile = ingestionFlowFileClient.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    String filePath = getFilePath(organizationId, ingestionFlowFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(
      filePath,
      ingestionFlowFile.getFileName());

    if (decryptedInputStream == null) {
      log.error("downloadIngestionFlowFile - File [{}] could not be decrypted or was not found", ingestionFlowFile.getFileName());
      throw new FileDecryptionException("File could not be decrypted or was not found");
    }

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), ingestionFlowFile.getFileName());
  }

  private String getFilePath(Long organizationId, IngestionFlowFile ingestionFlowFile) {
    Path organizationBasePath = fileStorerService.buildOrganizationBasePath(organizationId);
    if (organizationBasePath == null) {
      log.error("Organization base path is null for organizationId: {}", organizationId);
      throw new IllegalStateException("Organization base path cannot be null.");
    }

    String filePath;

    if (ingestionFlowFile.getStatus() == COMPLETED || ingestionFlowFile.getStatus() == ERROR) {
      filePath = String.format("%s/%s/%s/%s", organizationBasePath, ingestionFlowFile.getFilePathName(), archivedSubFolder, ingestionFlowFile.getFileName());
    } else {
      filePath = String.format("%s/%s/%s", organizationBasePath, ingestionFlowFile.getFilePathName(), ingestionFlowFile.getFileName());
    }
    return filePath;
  }

}
