package it.gov.pagopa.pu.fileshare.controller;


import it.gov.pagopa.pu.fileshare.controller.generated.SendFilesApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.send.SendFileFacadeService;
import it.gov.pagopa.pu.fileshare.util.Utilities;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class SendFilesController implements SendFilesApi {

  private final SendFileFacadeService sendFileFacadeService;

  public SendFilesController(SendFileFacadeService sendFileFacadeService) {
    this.sendFileFacadeService = sendFileFacadeService;
  }

  @Override
  public ResponseEntity<Resource> downloadSendFile(Long organizationId, String sendNotificationId, String filePath) {
    log.info("Requesting send file for [sendNotificationId: {}] of organization [organizationId: {}]", sendNotificationId, organizationId);
    FileResourceDTO fileResourceDTO = sendFileFacadeService.downloadSendFile(organizationId, sendNotificationId, filePath,
      SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());
    return Utilities.buildResourceResponseEntity(fileResourceDTO);
  }

  @Override
  public ResponseEntity<StartNotificationResponse> uploadSendFile(Long organizationId, String sendNotificationId,
    String digest, MultipartFile sendFile) {
    StartNotificationResponse response = sendFileFacadeService.uploadSendFile(organizationId, sendNotificationId, digest, sendFile,
      SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());
    if(response!=null)
      return new ResponseEntity<>(response, HttpStatus.OK);
    else
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
