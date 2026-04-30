package it.gov.pagopa.pu.fileshare.controller;

import it.gov.pagopa.pu.fileshare.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.mapper.UpstreamErrorMapper;
import it.gov.pagopa.pu.fileshare.security.JwtAuthenticationFilter;
import it.gov.pagopa.pu.fileshare.security.SecurityUtilsTest;
import it.gov.pagopa.pu.fileshare.service.receipt.ReceiptRtRetrieverService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ReceiptApi.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class ReceiptControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReceiptRtRetrieverService serviceMock;
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
  void whenDownloadRtThenReturnFile() throws Exception {
    Long organizationId = 1L;
    Long receiptId = 123L;
    String fileName = "test.txt";
    String fileContent = "this is a test file";

    FileResourceDTO fileResourceDTO = new FileResourceDTO();
    fileResourceDTO.setFileName(fileName);
    fileResourceDTO.setResourceStream(new InputStreamResource(new ByteArrayInputStream(fileContent.getBytes())));

    Mockito.when(serviceMock.downloadRt(Mockito.eq(organizationId), Mockito.eq(receiptId),
        Mockito.same(loggedUser), Mockito.same(accessToken)))
      .thenReturn(fileResourceDTO);

    mockMvc.perform(get("/organization/{organizationId}/rt/{receiptId}", organizationId, receiptId)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
      .andExpect(status().isOk())
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
      .andExpect(content().string(fileContent));
  }

}
