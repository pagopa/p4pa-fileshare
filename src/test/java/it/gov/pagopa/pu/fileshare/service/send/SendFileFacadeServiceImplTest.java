package it.gov.pagopa.pu.fileshare.service.send;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.fileshare.connector.send_notification.NotificationService;
import it.gov.pagopa.pu.fileshare.exception.custom.FileUploadException;
import it.gov.pagopa.pu.fileshare.exception.custom.InvalidFileException;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.service.UserAuthorizationService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.LoadFileRequest;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(sendFileFacadeService, "sendFolder", SEND_FOLDER);
    ReflectionTestUtils.setField(sendFileFacadeService, "archivedSubFolder", ARCHIVED_SUBFOLDER);
  }

  @Test
  void givenValidRequestWhenUploadSendFileThenSuccess() throws IOException {
    // Given
    String expectedFileName = SEND_NOTIFICATION_ID + "_" + FILE_NAME;
    StartNotificationResponse expectedResponse = new StartNotificationResponse();
    InputStream mockInputStream = new ByteArrayInputStream("TEST FILE HASH P4PA SEND".getBytes());

    // When
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getInputStream()).thenReturn(mockInputStream);
    when(fileStorerService.buildOrganizationBasePath(ORGANIZATION_ID))
      .thenReturn(Path.of("org", ORGANIZATION_ID.toString()));

    when(notificationService.startNotification(eq(SEND_NOTIFICATION_ID), eq(ORGANIZATION_ID), any(
      LoadFileRequest.class), eq(ACCESS_TOKEN)))
      .thenReturn(expectedResponse);

    StartNotificationResponse result = sendFileFacadeService.uploadSendFile(
      ORGANIZATION_ID, SEND_NOTIFICATION_ID, VALID_DIGEST, multipartFile, userInfo, ACCESS_TOKEN
    );

    // Then
    verify(userAuthorizationService).checkUserAuthorization(ORGANIZATION_ID, userInfo, ACCESS_TOKEN);
    verify(fileService).validateFile(multipartFile);
    verify(fileStorerService).saveToSharedFolder(ORGANIZATION_ID, multipartFile, SEND_FOLDER, expectedFileName);
    assertEquals(expectedResponse, result);
  }

  @Test
  void givenAlreadyUploadedWhenUploadSendFileThenSuccess() {
    MockMultipartFile file = new MockMultipartFile(
      "sendFile",
      "orginalFileName.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "TEST FILE HASH P4PA SEND".getBytes()
    );

    Path organizationBasePath = Path.of("/organizationFolder");
    StartNotificationResponse expectedResponse = new StartNotificationResponse();

    // When
    Mockito.when(fileStorerService.buildOrganizationBasePath(ORGANIZATION_ID))
      .thenReturn(organizationBasePath);
    try (MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class)) {
      filesMockedStatic.when(() -> Files.exists(
          organizationBasePath
            .resolve(SEND_FOLDER)
            .resolve(FILE_NAME + ".cipher")))
        .thenReturn(true);

      when(notificationService.startNotification(eq(SEND_NOTIFICATION_ID), eq(ORGANIZATION_ID), any(
        LoadFileRequest.class), eq(ACCESS_TOKEN)))
        .thenReturn(expectedResponse);

      StartNotificationResponse result = sendFileFacadeService.uploadSendFile(
        ORGANIZATION_ID, SEND_NOTIFICATION_ID, VALID_DIGEST, file, userInfo, ACCESS_TOKEN
      );

      verify(userAuthorizationService).checkUserAuthorization(ORGANIZATION_ID, userInfo, ACCESS_TOKEN);
      verify(fileService).validateFile(file);
      verify(fileStorerService, never()).saveToSharedFolder(ORGANIZATION_ID, file, SEND_FOLDER, "orginalFileName.txt");
      assertEquals(expectedResponse, result);
    }
  }

  @Test
  void givenInvalidFileDigestWhenUploadSendFileThenInvalidDigest()
    throws IOException {
    // GIVEN
    String content = "TEST FILE HASH P4PA SEND";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(
      StandardCharsets.UTF_8));
    // WHEN
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    // THEN
    assertThrows(InvalidFileException.class, () ->
      sendFileFacadeService.uploadSendFile(
        ORGANIZATION_ID, SEND_NOTIFICATION_ID, "WRONGDIGEST", multipartFile,
        userInfo, ACCESS_TOKEN
      )
    );
  }

  @Test
  void givenInvalidFileWhenUploadSendFileThenIOException() throws IOException {
    // Given
    when(multipartFile.getInputStream()).thenThrow(new IOException("Test IO Exception"));

    // Then
    assertThrows(FileUploadException.class, () ->
      sendFileFacadeService.uploadSendFile(
        ORGANIZATION_ID, SEND_NOTIFICATION_ID, VALID_DIGEST, multipartFile, userInfo, ACCESS_TOKEN
      )
    );
  }
}
