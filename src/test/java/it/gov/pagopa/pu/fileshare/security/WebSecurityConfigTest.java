package it.gov.pagopa.pu.fileshare.security;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.pu.fileshare.mapper.UpstreamErrorMapper;
import it.gov.pagopa.pu.fileshare.service.AuthorizationService;
import it.gov.pagopa.pu.fileshare.service.FileService;
import it.gov.pagopa.pu.fileshare.service.export.ExportFileFacadeService;
import it.gov.pagopa.pu.fileshare.service.ingestion.IngestionFlowFileFacadeService;
import it.gov.pagopa.pu.fileshare.service.receipt.ReceiptRtRetrieverService;
import it.gov.pagopa.pu.fileshare.service.send.SendFileFacadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
  classes = JwtAuthenticationFilter.class) )
@Import(WebSecurityConfig.class)
class WebSecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthorizationService authorizationServiceMock;
  @MockitoBean
  private IngestionFlowFileFacadeService ingestionFlowFileFacadeServiceMock;
  @MockitoBean
  private ExportFileFacadeService exportFileFacadeServiceMock;
  @MockitoBean
  private SendFileFacadeService sendFileFacadeServiceMock;
  @MockitoBean
  private ReceiptRtRetrieverService receiptRtRetrieverServiceMock;
  @MockitoBean
  private FileService fileServiceMock;
  @MockitoBean
  private UpstreamErrorMapper upstreamErrorMapperMock;

  @Test
  void givenURLWhenWithoutAccessTokenThenReturn403() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/notFound"))
      .andExpect(status().is4xxClientError());
  }

}
