package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileFacadeService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(value = IngestionFlowFileApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class IngestionFlowFilesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private IngestionFlowFileFacadeService serviceMock;

  @Test
  void givenCorrectRequestWhenUploadIngestionFlowFileThenOk() throws Exception {
    long organizationId = 1L;
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    Mockito.when(serviceMock.uploadIngestionFlowFile(Mockito.eq(organizationId),
        Mockito.eq(IngestionFlowFileType.RECEIPT), Mockito.eq(FileOrigin.PAGOPA), Mockito.eq(fileName),
        Mockito.eq(file), Mockito.any(), Mockito.anyString()))
      .thenReturn(1L);

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles",organizationId)
        .file(file)
        .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
        .param("fileOrigin", FileOrigin.PAGOPA.toString())
        .param("fileName", fileName)
        .contentType(MediaType.MULTIPART_FORM_DATA)
      ).andExpect(status().isOk())
      .andExpect(content().json("{\"ingestionFlowFileId\":1}"));
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

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles",organizationId)
      .file(file)
      .param("ingestionFlowFileType", "WrongValue")
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .param("fileName", file.getName())
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNoFileWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    String fileName = "fileName.txt";
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles",organizationId)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .param("fileName",fileName)
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNoIngestionFlowFileTypeWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles",organizationId)
      .file(file)
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .param("fileName",fileName)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenInvalidFileOriginWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles",organizationId)
      .file(file)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .param("fileOrigin", "WrongValue")
      .param("fileName",fileName)
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNoFileOriginWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );
    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles",organizationId)
      .file(file)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .param("fileName",fileName)
    ).andExpect(status().is4xxClientError());

    Mockito.verify(serviceMock, Mockito.times(0)).uploadIngestionFlowFile(Mockito.any(),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenCorrectRequestWhenDownloadIngestionFlowFileThenReturnFile() throws Exception {
    Long organizationId = 1L;
    Long ingestionFlowFileId = 123L;
    String fileName = "test.txt";
    String fileContent = "this is a test file";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    Mockito.when(serviceMock.downloadIngestionFlowFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
        Mockito.any(), Mockito.anyString()))
      .thenReturn(fileResourceDTO);

    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(get("/organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}", organizationId, ingestionFlowFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));

    Mockito.verify(serviceMock).downloadIngestionFlowFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
      Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNonExistentFileWhenDownloadIngestionFlowFileThenReturnNotFound() throws Exception {
    Long organizationId = 1L;
    Long ingestionFlowFileId = 123L;

    Mockito.when(serviceMock.downloadIngestionFlowFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
        Mockito.any(), Mockito.anyString()))
      .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(get("/organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}", organizationId, ingestionFlowFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isNotFound());

    Mockito.verify(serviceMock).downloadIngestionFlowFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
      Mockito.any(), Mockito.anyString());
  }

}
