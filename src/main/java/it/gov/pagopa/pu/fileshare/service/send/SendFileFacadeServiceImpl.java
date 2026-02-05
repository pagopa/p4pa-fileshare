package it.gov.pagopa.pu.fileshare.service.send;

import it.gov.pagopa.pu.fileshare.connector.send_notification.NotificationService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.exception.custom.OrganizationMissMatchException;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.fileshare.util.AESUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.SendNotificationDTO;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static it.gov.pagopa.pu.fileshare.service.FileStorerService.concatenatePaths;

@Slf4j
@Service
public class SendFileFacadeServiceImpl implements SendFileFacadeService {

  private final UserAuthorizationService userAuthorizationService;
  private final FileService fileService;
  private final FileStorerService fileStorerService;
  private final NotificationService notificationService;
  private final String sendFolder;
  private final String archivedSubFolder;


  public SendFileFacadeServiceImpl(
    UserAuthorizationService userAuthorizationService, FileService fileService,
    FileStorerService fileStorerService,
    NotificationService notificationService,
    @Value("${folders.send-file-folder}") String sendFolder,
    @Value("${folders.process-target-sub-folders.archive}") String archivedSubFolder) {
    this.userAuthorizationService = userAuthorizationService;
    this.fileService = fileService;
    this.fileStorerService = fileStorerService;
    this.notificationService = notificationService;
    this.sendFolder = sendFolder;
    this.archivedSubFolder = archivedSubFolder;
  }

  @Override
  public StartNotificationResponse uploadSendFile(
    Long organizationId, String sendNotificationId, String digest,
    MultipartFile sendFile, UserInfo user, String accessToken
  ) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    validateSendNotification(organizationId, sendNotificationId, accessToken);

    fileService.validateFile(sendFile);
    String fileName = sendNotificationId+"_"+sendFile.getOriginalFilename();

    String relativeSubFolder = Path.of(sendFolder).resolve(String.valueOf(sendNotificationId)).toString();
    if(!fileStorerService.checkIfAlreadyUploadedOrArchived(organizationId,archivedSubFolder, relativeSubFolder, fileName)) {
      validateDigestAndSave(organizationId, sendFile, relativeSubFolder, fileName, digest);
    }

    LoadFileRequest fileRequest = LoadFileRequest.builder().fileName(sendFile.getOriginalFilename()).digest(digest).build();
    return notificationService.startNotification(sendNotificationId, fileRequest, accessToken);
  }

  @Override
  public FileResourceDTO downloadSendFile(Long organizationId, String sendNotificationId, String pathFile, UserInfo user, String accessToken) {
    userAuthorizationService.checkUserAuthorization(organizationId, user, accessToken);
    validateSendNotification(organizationId, sendNotificationId, accessToken);
    Path path = Path.of(pathFile);
    String fileName = path.getFileName().toString();

    InputStream decryptedInputStream = fileStorerService.decryptFile(path.getParent(), fileName);

    return new FileResourceDTO(new InputStreamResource(decryptedInputStream), fileName);
  }

  private void validateSendNotification(Long organizationId, String sendNotificationId, String accessToken) {
    SendNotificationDTO sendNotification = notificationService.getSendNotification(sendNotificationId, accessToken);
    if (!sendNotification.getOrganizationId().equals(organizationId)) {
      throw new OrganizationMissMatchException("INVALID_SEND_NOTIFICATION_ORG_MISMATCH", "Requested sendNotificationId (" + sendNotificationId + ") not exists under requested organization " + organizationId);
    }
  }

  private void validateDigestAndSave(Long organizationId, MultipartFile sendFile,
    String relativeSendSubFolder, String fileName, String digest) {
    try {
      SaveFileResultDTO resultDTO = fileStorerService.saveToSharedFolder(
        organizationId, sendFile, relativeSendSubFolder, fileName);
      if (!digest.equals(Base64.getEncoder().encodeToString(resultDTO.getFileHash()))) {
        Path relativeFileLocation = concatenatePaths(resultDTO.getRelativePath(), fileName + AESUtils.CIPHER_EXTENSION);
        Path organizationBasePath = fileStorerService.buildOrganizationBasePath(organizationId);
        Path absolutePath = concatenatePaths(organizationBasePath.toString(), relativeFileLocation.toString());
        Files.deleteIfExists(absolutePath);
        throw new InvalidFileException("INVALID_DIGEST", "Invalid digest");
      }
    } catch (IOException e){
      throw new FileUploadException("FILE_UPLOADING_ERROR", e.getMessage());
    }
  }
}
