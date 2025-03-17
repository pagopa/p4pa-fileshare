package it.gov.pagopa.pu.fileshare.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.pu.fileshare.controller.generated.SendFileApi;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.service.send.SendFileFacadeService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.sendnotification.dto.generated.StartNotificationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = SendFileApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class SendFilesControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private SendFileFacadeService serviceMock;

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
    TestUtils.addSampleUserIntoSecurityContext();

    StartNotificationResponse expectedResponse = StartNotificationResponse.builder().workFlowId("ID").build();

    Mockito.when(serviceMock.uploadSendFile(Mockito.eq(organizationId),
        Mockito.eq(sendNotificationId), Mockito.eq(digest), Mockito.eq(file), Mockito.any(), Mockito.anyString()))
      .thenReturn(expectedResponse);

    mockMvc.perform(multipart("/organization/{organizationId}/uploadsendfiles/{sendNotificationId}",organizationId, sendNotificationId)
        .file(file)
        .param("digest", digest)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"workFlowId\":ID}"));
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
    TestUtils.addSampleUserIntoSecurityContext();

    Mockito.when(serviceMock.uploadSendFile(Mockito.eq(organizationId),
        Mockito.eq(sendNotificationId), Mockito.eq(digest), Mockito.eq(file), Mockito.any(), Mockito.anyString()))
      .thenReturn(null);

    mockMvc.perform(multipart("/organization/{organizationId}/uploadsendfiles/{sendNotificationId}",organizationId, sendNotificationId)
        .file(file)
        .param("digest", digest)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isAccepted());
  }
}
