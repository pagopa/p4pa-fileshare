package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.SendFileApi;
import it.gov.pagopa.pu.fileshare.security.SecurityUtils;
import it.gov.pagopa.pu.fileshare.service.send.SendFileFacadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SendFilesController implements SendFileApi {

  private final SendFileFacadeService sendFileFacadeService;

  public SendFilesController(SendFileFacadeService sendFileFacadeService) {
    this.sendFileFacadeService = sendFileFacadeService;
  }

  @Override
  public ResponseEntity<Void> uploadSendFile(Long organizationId, String sendNotificationId,
    String digest, MultipartFile sendFile) {
    sendFileFacadeService.uploadSendFile(organizationId, sendNotificationId, digest, sendFile,
      SecurityUtils.getLoggedUser(), SecurityUtils.getAccessToken());
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
