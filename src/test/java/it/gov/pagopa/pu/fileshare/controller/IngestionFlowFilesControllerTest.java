package it.gov.pagopa.pu.fileshare.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
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

@WebMvcTest(value = IngestionFlowFileApi.class,excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class IngestionFlowFilesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private IngestionFlowFileService serviceMock;

  @Test
  void givenCorrectRequestWhenUploadIngestionFlowFileThenOk() throws Exception {
    long organizationId = 1L;
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/ingestionflowfiles/"+organizationId)
        .file(file)
        .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
        .param("fileOrigin", FileOrigin.PAGOPA.toString())
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isOk());

    Mockito.verify(serviceMock).uploadIngestionFlowFile(Mockito.eq(organizationId),
      Mockito.eq(IngestionFlowFileType.RECEIPT),Mockito.eq(FileOrigin.PAGOPA),Mockito.eq(file),
      Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenInvalidIngestionFlowFileTypeWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/ingestionflowfiles/"+organizationId)
      .file(file)
      .param("ingestionFlowFileType", "WrongValue")
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNoFileWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/ingestionflowfiles/"+organizationId)
        .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
        .param("fileOrigin", FileOrigin.PAGOPA.toString())
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNoIngestionFlowFileTypeWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/ingestionflowfiles/"+organizationId)
      .file(file)
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenInvalidFileOriginWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/ingestionflowfiles/"+organizationId)
      .file(file)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .param("fileOrigin", "WrongValue")
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNoFileOriginWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/ingestionflowfiles/"+organizationId)
      .file(file)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any(), Mockito.anyString());
  }
}
