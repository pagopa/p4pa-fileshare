package it.gov.pagopa.pu.fileshare.service.send;

import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.springframework.web.multipart.MultipartFile;

public interface SendFileFacadeService {
  void uploadSendFile(Long organizationId, String sendNotificationId, String digest, MultipartFile sendFile,
    UserInfo user, String accessToken);
}
