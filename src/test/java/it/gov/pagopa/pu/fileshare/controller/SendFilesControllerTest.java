package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.SendFilesApi;
import it.gov.pagopa.pu.fileshare.mapper.UpstreamErrorMapper;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.security.SecurityUtilsTest;
import it.gov.pagopa.pu.fileshare.service.send.SendFileFacadeService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SendFilesApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class SendFilesControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private SendFileFacadeService serviceMock;
  @MockitoBean
  private UpstreamErrorMapper upstreamErrorMapperMock;

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

    Mockito.when(serviceMock.uploadSendFile(Mockito.eq(organizationId),
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

    Mockito.when(serviceMock.uploadSendFile(Mockito.eq(organizationId),
        Mockito.eq(sendNotificationId), Mockito.eq(digest), Mockito.eq(file),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(null);

    mockMvc.perform(multipart("/organization/{organizationId}/send-files/{sendNotificationId}",organizationId, sendNotificationId)
        .file(file)
        .param("digest", digest)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isAccepted());
  }
}
