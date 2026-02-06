package it.gov.pagopa.pu.fileshare.service.send;

import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SendFileFacadeService {
  StartNotificationResponse uploadSendFile(Long organizationId, String sendNotificationId, String digest, MultipartFile sendFile,
    UserInfo user, String accessToken);
  FileResourceDTO downloadSendFile(Long organizationId, String sendNotificationId, String pathFile, UserInfo user, String accessToken);
}
