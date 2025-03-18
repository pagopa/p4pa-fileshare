package it.gov.pagopa.pu.fileshare.service.send;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.fileshare.connector.send_notification.NotificationService;
import it.gov.pagopa.pu.fileshare.dto.SaveFileResultDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import java.nio.file.Paths;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class SendFileFacadeServiceImplTest {

  @Mock
  private UserAuthorizationService userAuthorizationService;
  @Mock
  private FileService fileService;
  @Mock
  private FileStorerService fileStorerService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private MultipartFile multipartFile;
  @Mock
  private UserInfo userInfo;

  @InjectMocks
  private SendFileFacadeServiceImpl sendFileFacadeService;

  private static final String SEND_FOLDER = "send";
  private static final String ARCHIVED_SUBFOLDER = "archived";
  private static final Long ORGANIZATION_ID = 1L;
  private static final String SEND_NOTIFICATION_ID = "notification123";
  private static final String FILE_NAME = "test.txt";
  private static final String ACCESS_TOKEN = "token123";
  private static final String VALID_DIGEST = "9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=";
  private static final String FILE_CONTENT = "TEST FILE HASH P4PA SEND";
  private static final String FINAL_FILE_NAME = SEND_NOTIFICATION_ID + "_" + FILE_NAME;
  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(sendFileFacadeService, "sendFolder", SEND_FOLDER);
    ReflectionTestUtils.setField(sendFileFacadeService, "archivedSubFolder", ARCHIVED_SUBFOLDER);
  }

  @Test
  void givenValidRequestWhenUploadSendFileThenSuccess() {
    // Given
    StartNotificationResponse expectedResponse = new StartNotificationResponse();
    byte[] expectedFileHash = FILE_CONTENT.getBytes(); // This should match the valid hash
    SaveFileResultDTO saveFileResult = new SaveFileResultDTO("/organization", expectedFileHash);

    // When
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(fileStorerService.saveToSharedFolder(ORGANIZATION_ID, multipartFile, SEND_FOLDER, FINAL_FILE_NAME))
      .thenReturn(saveFileResult);

    String validDigest = Base64.getEncoder().encodeToString(expectedFileHash);

    // The valid digest used in the uploadSendFile method call
    when(notificationService.startNotification(eq(SEND_NOTIFICATION_ID), eq(ORGANIZATION_ID), any(
      LoadFileRequest.class), eq(ACCESS_TOKEN)))
      .thenReturn(expectedResponse);

    StartNotificationResponse result = sendFileFacadeService.uploadSendFile(
      ORGANIZATION_ID, SEND_NOTIFICATION_ID, validDigest, multipartFile, userInfo, ACCESS_TOKEN
    );

    // Then
    verify(userAuthorizationService).checkUserAuthorization(ORGANIZATION_ID, userInfo, ACCESS_TOKEN);
    verify(fileService).validateFile(multipartFile);
    verify(fileStorerService).saveToSharedFolder(ORGANIZATION_ID, multipartFile, SEND_FOLDER, FINAL_FILE_NAME);
    assertEquals(expectedResponse, result);
  }

  @Test
  void givenAlreadyUploadedWhenUploadSendFileThenSuccess() {
    MockMultipartFile file = new MockMultipartFile(
      "sendFile",
      FILE_NAME,
      MediaType.TEXT_PLAIN_VALUE,
      FILE_CONTENT.getBytes()
    );

    StartNotificationResponse expectedResponse = new StartNotificationResponse();

    when(fileStorerService.checkIfAlreadyUploadedOrArchived(
      ORGANIZATION_ID, ARCHIVED_SUBFOLDER, SEND_FOLDER, FINAL_FILE_NAME))
      .thenReturn(true);

    when(notificationService.startNotification(eq(SEND_NOTIFICATION_ID), eq(ORGANIZATION_ID), any(
      LoadFileRequest.class), eq(ACCESS_TOKEN)))
      .thenReturn(expectedResponse);

    StartNotificationResponse result = sendFileFacadeService.uploadSendFile(
      ORGANIZATION_ID, SEND_NOTIFICATION_ID, VALID_DIGEST, file, userInfo, ACCESS_TOKEN
    );

    verify(userAuthorizationService).checkUserAuthorization(ORGANIZATION_ID, userInfo, ACCESS_TOKEN);
    verify(fileService).validateFile(file);
    verify(fileStorerService, never()).saveToSharedFolder(ORGANIZATION_ID, file, SEND_FOLDER, FINAL_FILE_NAME);
    assertEquals(expectedResponse, result);

  }

  @Test
  void givenInvalidFileDigestWhenUploadSendFileThenInvalidDigest() {
    // GIVEN
    MockMultipartFile file = new MockMultipartFile(
      "sendFile",
      FILE_NAME,
      MediaType.TEXT_PLAIN_VALUE,
      FILE_CONTENT.getBytes()
    );

    SaveFileResultDTO saveFileResultDTO = new SaveFileResultDTO();
    saveFileResultDTO.setFileHash("wrongHash".getBytes());
    saveFileResultDTO.setRelativePath("path");

    // WHEN
    when(fileStorerService.checkIfAlreadyUploadedOrArchived(
      ORGANIZATION_ID, ARCHIVED_SUBFOLDER, SEND_FOLDER, FINAL_FILE_NAME))
      .thenReturn(false);

    when(fileStorerService.saveToSharedFolder(
      ORGANIZATION_ID, file, SEND_FOLDER, FINAL_FILE_NAME))
      .thenReturn(saveFileResultDTO);

    when(fileStorerService.buildOrganizationBasePath(ORGANIZATION_ID))
      .thenReturn(Paths.get("basePath"));

    // THEN
    assertThrows(InvalidFileException.class, () ->
      sendFileFacadeService.uploadSendFile(
        ORGANIZATION_ID, SEND_NOTIFICATION_ID, VALID_DIGEST, file,
        userInfo, ACCESS_TOKEN
      )
    );
  }

  @Test
  void givenInvalidFileWhenUploadSendFileThenFileUploadException() {
    // When
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(fileStorerService.checkIfAlreadyUploadedOrArchived(
      ORGANIZATION_ID, ARCHIVED_SUBFOLDER, SEND_FOLDER, FINAL_FILE_NAME))
      .thenReturn(false);
    when(fileStorerService.saveToSharedFolder(
      ORGANIZATION_ID, multipartFile, SEND_FOLDER, FINAL_FILE_NAME))
      .thenThrow(new FileUploadException("Error saving file"));

    // Act & Assert
    assertThrows(FileUploadException.class, () ->
      sendFileFacadeService.uploadSendFile(
        ORGANIZATION_ID,
        SEND_NOTIFICATION_ID,
        VALID_DIGEST,
        multipartFile,
        userInfo,
        ACCESS_TOKEN
      )
    );
  }
}
