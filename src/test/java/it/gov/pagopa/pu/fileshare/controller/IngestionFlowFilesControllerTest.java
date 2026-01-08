package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.IngestionFlowFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileOrigin;
import it.gov.pagopa.pu.fileshare.dto.generated.IngestionFlowFileType;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.security.SecurityUtilsTest;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileFacadeService;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = IngestionFlowFileApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class IngestionFlowFilesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private IngestionFlowFileFacadeService ingestionFlowFileFacadeServiceMock;

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
  void givenCorrectRequestWhenUploadIngestionFlowFileThenOk() throws Exception {
    long ingestionFlowFileId = 1L;
    long organizationId = 1L;
    String fileName = "fileName.txt";
    MockMultipartFile file = new MockMultipartFile(
      "ingestionFlowFile",
      "test.txt",
      MediaType.TEXT_PLAIN_VALUE,
      "this is a test file".getBytes()
    );

    Mockito.when(ingestionFlowFileFacadeServiceMock.uploadIngestionFlowFile(Mockito.eq(organizationId),
        Mockito.eq(IngestionFlowFileType.RECEIPT), Mockito.eq(FileOrigin.PAGOPA), Mockito.eq(fileName),
        Mockito.eq(file), Mockito.eq(ingestionFlowFileId),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(1L);

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles", organizationId)
        .file(file)
        .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
        .param("fileOrigin", FileOrigin.PAGOPA.toString())
        .param("fileName", fileName)
        .param("ingestionFlowFileId", String.valueOf(ingestionFlowFileId))
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

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles", organizationId)
      .file(file)
      .param("ingestionFlowFileType", "WrongValue")
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .param("fileName", file.getName())
      .param("ingestionFlowFileId", "1")
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verifyNoInteractions(ingestionFlowFileFacadeServiceMock);
  }

  @Test
  void givenNoFileWhenUploadIngestionFlowFileThenError() throws Exception {
    long organizationId = 1L;
    String fileName = "fileName.txt";

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles", organizationId)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .param("fileName", fileName)
      .param("ingestionFlowFileId", "1")
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verifyNoInteractions(ingestionFlowFileFacadeServiceMock);
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

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles", organizationId)
      .file(file)
      .param("fileOrigin", FileOrigin.PAGOPA.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .param("fileName", fileName)
      .param("ingestionFlowFileId", "1")
    ).andExpect(status().is4xxClientError());

    Mockito.verifyNoInteractions(ingestionFlowFileFacadeServiceMock);
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


    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles", organizationId)
      .file(file)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .param("fileOrigin", "WrongValue")
      .param("fileName", fileName)
      .param("ingestionFlowFileId", "1")
      .contentType(MediaType.MULTIPART_FORM_DATA)
    ).andExpect(status().is4xxClientError());

    Mockito.verifyNoInteractions(ingestionFlowFileFacadeServiceMock);
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

    mockMvc.perform(multipart("/organization/{organizationId}/ingestionflowfiles", organizationId)
      .file(file)
      .param("ingestionFlowFileType", IngestionFlowFileType.RECEIPT.toString())
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .param("fileName", fileName)
      .param("ingestionFlowFileId", "1")
    ).andExpect(status().is4xxClientError());

    Mockito.verifyNoInteractions(ingestionFlowFileFacadeServiceMock);
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

    Mockito.when(ingestionFlowFileFacadeServiceMock.downloadIngestionFlowFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(fileResourceDTO);

    mockMvc.perform(get("/organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}", organizationId, ingestionFlowFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));

  }

  @Test
  void givenCorrectRequestWhenDownloadIngestionFlowErrorsFileThenReturnFile() throws Exception {
    Long organizationId = 1L;
    Long ingestionFlowFileId = 123L;
    String fileName = "errorsTest.txt";
    String fileContent = "this is a test file";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    Mockito.when(ingestionFlowFileFacadeServiceMock.downloadIngestionFlowErrorsFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(fileResourceDTO);

    mockMvc.perform(get("/organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}/errors", organizationId, ingestionFlowFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));

  }

  @Test
  void whenDownloadNoticeThenOk() throws Exception {
    Long organizationId = 1L;
    Long ingestionFlowFileId = 123L;
    String fileName = "notice.txt";
    String fileContent = "this is a test file";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    Mockito.when(ingestionFlowFileFacadeServiceMock.downloadNotice(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(fileResourceDTO);

    mockMvc.perform(get("/organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}/notice", organizationId, ingestionFlowFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));

  }

  @Test
  void whenDownloadIuvFileThenOk() throws Exception {
    Long organizationId = 1L;
    Long ingestionFlowFileId = 123L;
    String fileName = "notice.txt";
    String fileContent = "this is a test file";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    Mockito.when(ingestionFlowFileFacadeServiceMock.downloadIuvFile(Mockito.eq(organizationId), Mockito.eq(ingestionFlowFileId),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(fileResourceDTO);

    mockMvc.perform(get("/organization/{organizationId}/ingestionflowfiles/{ingestionFlowFileId}/iuv", organizationId, ingestionFlowFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));

  }
}
