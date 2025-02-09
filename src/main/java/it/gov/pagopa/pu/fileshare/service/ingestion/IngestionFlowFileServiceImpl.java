package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.exception.custom.FileAlreadyExistsException;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
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
  public Long uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType,
                                        FileOrigin fileOrigin, MultipartFile ingestionFlowFile, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    fileService.validateFile(ingestionFlowFile, validIngestionFlowFileExt);

    String fileName = ingestionFlowFile.getOriginalFilename();
    String ingestionFlowFilePath = foldersPathsConfig.getIngestionFlowFilePath(ingestionFlowFileType);

    if(checkIfAlreadyUploadedOrArchived(organizationId, ingestionFlowFilePath, fileName)) {
      throw new FileAlreadyExistsException("File already uploaded or archived");
    }

    String filePath = fileStorerService.saveToSharedFolder(organizationId, ingestionFlowFile,
      ingestionFlowFilePath, fileName);

    return ingestionFlowFileClient.createIngestionFlowFile(
      ingestionFlowFileDTOMapper.mapToIngestionFlowFileDTO(ingestionFlowFile,
        ingestionFlowFileType, fileOrigin, organizationId, filePath)
      , accessToken);
  }

  @Override
  public FileResourceDTO downloadIngestionFlowFile(Long organizationId, Long ingestionFlowFileId, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);

    IngestionFlowFile ingestionFlowFile = ingestionFlowFileClient.getIngestionFlowFile(ingestionFlowFileId, accessToken);

    Path filePath = getFilePath(ingestionFlowFile);

    InputStream decryptedInputStream = fileStorerService.decryptFile(filePath, ingestionFlowFile.getFileName());

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), ingestionFlowFile.getFileName());
  }

  private Path getFilePath(IngestionFlowFile ingestionFlowFile) {
    Path organizationBasePath = fileStorerService.buildOrganizationBasePath(ingestionFlowFile.getOrganizationId());

    Path filePath = organizationBasePath
      .resolve(ingestionFlowFile.getFilePathName());
    if (ingestionFlowFile.getStatus() == COMPLETED || ingestionFlowFile.getStatus() == ERROR) {
      filePath = filePath
        .resolve(archivedSubFolder);
    }
    return filePath;
  }

  private boolean checkIfAlreadyUploadedOrArchived(Long organizationId, String ingestionFlowFilePath, String fileName) {
    Path filePath = fileStorerService.buildOrganizationBasePath(organizationId)
      .resolve(ingestionFlowFilePath);
    String fileNameCiphered = fileName + AESUtils.CIPHER_EXTENSION;
    return Files.exists(filePath.resolve(fileNameCiphered))
      || Files.exists(filePath.resolve(archivedSubFolder).resolve(fileNameCiphered));
  }

}
