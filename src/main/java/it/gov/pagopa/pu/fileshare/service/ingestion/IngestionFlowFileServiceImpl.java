package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.config.FoldersPathsConfig;
import it.gov.pagopa.pu.fileshare.connector.processexecutions.client.IngestionFlowFileClient;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.mapper.IngestionFlowFileDTOMapper;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

  public IngestionFlowFileServiceImpl(
    UserAuthorizationService userAuthorizationService, FileService fileService,
    FileStorerService fileStorerService,
    FoldersPathsConfig foldersPathsConfig,
    IngestionFlowFileClient ingestionFlowFileClient,
    IngestionFlowFileDTOMapper ingestionFlowFileDTOMapper,
    @Value("${uploads.ingestion-flow-file.valid-extension}") String validIngestionFlowFileExt
    ) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileService = fileService;
    this.fileStorerService = fileStorerService;
    this.foldersPathsConfig = foldersPathsConfig;
    this.ingestionFlowFileClient = ingestionFlowFileClient;
    this.ingestionFlowFileDTOMapper = ingestionFlowFileDTOMapper;
    this.validIngestionFlowFileExt = validIngestionFlowFileExt;
  }

  @Override
  public String uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType,
    FileOrigin fileOrigin, MultipartFile ingestionFlowFile, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    fileService.validateFile(ingestionFlowFile, validIngestionFlowFileExt);
    String filePath = fileStorerService.saveToSharedFolder(ingestionFlowFile,
      foldersPathsConfig.getIngestionFlowFilePath(ingestionFlowFileType));

    return ingestionFlowFileClient.createIngestionFlowFile(
      ingestionFlowFileDTOMapper.mapToIngestionFlowFileDTO(ingestionFlowFile,
        ingestionFlowFileType, fileOrigin, organizationId, filePath)
      , accessToken);
  }
}
