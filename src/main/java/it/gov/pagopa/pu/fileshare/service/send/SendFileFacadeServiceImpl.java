package it.gov.pagopa.pu.fileshare.service.send;

import it.gov.pagopa.pu.fileshare.exception.custom.FileAlreadyExistsException;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import it.gov.pagopa.pu.fileshare.util.FileUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class SendFileFacadeServiceImpl implements SendFileFacadeService {

  private final UserAuthorizationService userAuthorizationService;
  private final FileService fileService;
  private final FileStorerService fileStorerService;
  private final String sendFolder;
  private final String archivedSubFolder;


  public SendFileFacadeServiceImpl(
    UserAuthorizationService userAuthorizationService, FileService fileService,
    FileStorerService fileStorerService,
    @Value("${folders.send-file-folder}") String sendFolder,
    @Value("${folders.process-target-sub-folders.archive}") String archivedSubFolder) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileService = fileService;
    this.fileStorerService = fileStorerService;
    this.sendFolder = sendFolder;
    this.archivedSubFolder = archivedSubFolder;
  }

  @Override
  public void uploadSendFile(Long organizationId, String sendNotificationId, String digest,
    MultipartFile sendFile, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    fileService.validateFile(sendFile);
    String fileName = sendNotificationId+"_"+sendFile.getOriginalFilename();
    try {
      if(!digest.equals(FileUtils.calculateFileHash(sendFile.getInputStream())))
        throw new InvalidFileException("Invalid digest");
    } catch (IOException | NoSuchAlgorithmException e) {
      throw new FileUploadException(e.getMessage());
    }

    if(checkIfAlreadyUploadedOrArchived(organizationId, sendFolder, fileName)) {
      throw new FileAlreadyExistsException("File already uploaded or archived");
    }
    fileStorerService.saveToSharedFolder(organizationId, sendFile, sendFolder, fileName);
    //call sendstartnotification
  }

  private boolean checkIfAlreadyUploadedOrArchived(Long organizationId, String sendFolder, String fileName) {
    Path filePath = fileStorerService.buildOrganizationBasePath(organizationId)
      .resolve(sendFolder);
    String fileNameCiphered = fileName + AESUtils.CIPHER_EXTENSION;
    return Files.exists(
      FileStorerService.concatenatePaths(filePath.toString(), fileNameCiphered))
      || Files.exists(FileStorerService.concatenatePaths(filePath.resolve(archivedSubFolder).toString(), fileNameCiphered));
  }
}
