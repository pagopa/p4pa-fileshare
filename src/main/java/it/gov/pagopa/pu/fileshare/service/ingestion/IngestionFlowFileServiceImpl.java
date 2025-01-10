package it.gov.pagopa.pu.fileshare.service.ingestion;

import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.service.FileService;
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
  private final String validIngestionFlowFileExt;

  public IngestionFlowFileServiceImpl(
    UserAuthorizationService userAuthorizationService, FileService fileService,
    @Value("${file.ingestion-flow-file.valid-extension}") String validIngestionFlowFileExt) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileService = fileService;
    this.validIngestionFlowFileExt = validIngestionFlowFileExt;
  }

  @Override
  public void uploadIngestionFlowFile(Long organizationId, IngestionFlowFileType ingestionFlowFileType, MultipartFile ingestionFlowFile, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    fileService.validateFile(ingestionFlowFile, validIngestionFlowFileExt);
  }
}
