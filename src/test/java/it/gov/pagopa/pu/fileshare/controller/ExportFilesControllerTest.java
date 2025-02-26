package it.gov.pagopa.pu.fileshare.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.pu.fileshare.controller.generated.ExportFileApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FlowFileNotFoundException;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.service.export.ExportFileFacadeService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ExportFileApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class ExportFilesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ExportFileFacadeService serviceMock;

  @Test
  void givenCorrectRequestWhenDownloadExportFileThenReturnFile() throws Exception {
    Long organizationId = 1L;
    Long exportFileId = 123L;
    String fileName = "test.txt";
    String fileContent = "this is a test file";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    Mockito.when(serviceMock.downloadExportFile(Mockito.eq(organizationId), Mockito.eq(exportFileId),
        Mockito.any(), Mockito.anyString()))
      .thenReturn(fileResourceDTO);

    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(get("/exportfiles/{organizationId}/{exportFileId}", organizationId, exportFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));

    Mockito.verify(serviceMock).downloadExportFile(Mockito.eq(organizationId), Mockito.eq(exportFileId),
      Mockito.any(), Mockito.anyString());
  }

  @Test
  void givenNonExistentFileWhenDownloadExportFileThenReturnNotFound() throws Exception {
    Long organizationId = 1L;
    Long exportFileId = 123L;

    Mockito.when(serviceMock.downloadExportFile(Mockito.eq(organizationId), Mockito.eq(exportFileId),
        Mockito.any(), Mockito.anyString()))
      .thenThrow(new FlowFileNotFoundException("File not found"));

    TestUtils.addSampleUserIntoSecurityContext();

    mockMvc.perform(get("/exportfiles/{organizationId}/{exportFileId}", organizationId, exportFileId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isNotFound());

    Mockito.verify(serviceMock).downloadExportFile(Mockito.eq(organizationId), Mockito.eq(exportFileId),
      Mockito.any(), Mockito.anyString());
  }

}
