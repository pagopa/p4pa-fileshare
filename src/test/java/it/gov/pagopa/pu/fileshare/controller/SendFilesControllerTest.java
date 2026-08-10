package it.gov.pagopa.pu.fileshare.controller;

import io.micrometer.tracing.Tracer;
import it.gov.pagopa.pu.fileshare.controller.generated.SendFilesApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.security.SecurityUtilsTest;
import it.gov.pagopa.pu.fileshare.service.send.SendFileFacadeService;
import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SendFilesApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class SendFilesControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private SendFileFacadeService serviceMock;
  @MockitoBean
  private Tracer tracerMock;

  private final String accessToken = "ACCESSTOKEN";
  private final UserInfo loggedUser = new UserInfo();

  @BeforeEach
  void init(){
    SecurityUtilsTest.configureSecurityContext(accessToken, loggedUser);
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @Test
  void givenStartNotificationRequestThenReturnWorkFlowId() throws Exception {
    Long organizationId = 1L;
    String sendNotificationId = "NOTIFICATIONID";
    String digest = "DIGEST";
    MockMultipartFile file = new MockMultipartFile(
      "sendFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    StartNotificationResponse expectedResponse = new StartNotificationResponse("ID", "RUNID");

    when(serviceMock.uploadSendFile(Mockito.eq(organizationId),
        Mockito.eq(sendNotificationId), Mockito.eq(digest), Mockito.eq(file),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(expectedResponse);

    mockMvc.perform(multipart("/organization/{organizationId}/send-files/{sendNotificationId}",organizationId, sendNotificationId)
        .file(file)
        .param("digest", digest)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"workflowId\":\"ID\",\"runId\":\"RUNID\"}"));
  }

  @Test
  void givenStartNotificationRequestThenAccepted() throws Exception {
    Long organizationId = 1L;
    String sendNotificationId = "NOTIFICATIONID";
    String digest = "DIGEST";
    MockMultipartFile file = new MockMultipartFile(
      "sendFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    when(serviceMock.uploadSendFile(Mockito.eq(organizationId),
        Mockito.eq(sendNotificationId), Mockito.eq(digest), Mockito.eq(file),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(null);

    mockMvc.perform(multipart("/organization/{organizationId}/send-files/{sendNotificationId}",organizationId, sendNotificationId)
        .file(file)
        .param("digest", digest)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isAccepted());
  }

  @Test
  void givenCorrectRequestWhenDownloadSendFileThenReturnFile() throws Exception {
    Long organizationId = 1L;
    String sendNotificationId = "NOTIFICATIONID";
    String fileName = "test.txt";
    String fileContent = "this is a test file";
    String filePath = "/shared/test.txt";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    when(serviceMock.downloadSendFile(Mockito.eq(organizationId), Mockito.eq(sendNotificationId), Mockito.eq(filePath),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(fileResourceDTO);

    mockMvc.perform(get("/organization/{organizationId}/send-files/{sendNotificationId}", organizationId, sendNotificationId)
        .queryParam("filePath", filePath)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));
  }

  @Test
  void givenNonExistentFileWhenDownloadSendFileThenReturnNotFound() throws Exception {
    Long organizationId = 1L;
    String sendNotificationId = "NOTIFICATIONID";
    String filePath = "/shared/test.txt";

    when(serviceMock.downloadSendFile(Mockito.eq(organizationId), Mockito.eq(sendNotificationId), Mockito.eq(filePath),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenThrow(new FileNotFoundException("FILE_NOT_FOUND", "File not found"));

    mockMvc.perform(get("/organization/{organizationId}/send-files/{sendNotificationId}", organizationId, sendNotificationId)
        .queryParam("filePath", filePath)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isNotFound());
  }
}
