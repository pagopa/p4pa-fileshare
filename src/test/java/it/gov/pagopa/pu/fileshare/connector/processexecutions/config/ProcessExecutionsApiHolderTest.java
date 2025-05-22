package it.gov.pagopa.pu.fileshare.connector.processexecutions.config;

import it.gov.pagopa.pu.fileshare.connector.BaseApiHolderTest;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileRequestDTO;
import it.gov.pagopa.pu.p4paprocessexecutions.dto.generated.IngestionFlowFileStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class ProcessExecutionsApiHolderTest extends BaseApiHolderTest {
  @Mock
  private RestTemplateBuilder restTemplateBuilderMock;

  private ProcessExecutionsApisHolder processExecutionsApisHolder;

  @BeforeEach
  void setUp() {
    Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
    Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
    ProcessExecutionsApiClientConfig clientConfig = ProcessExecutionsApiClientConfig.builder()
      .baseUrl("http://example.com")
      .build();
    processExecutionsApisHolder = new ProcessExecutionsApisHolder(clientConfig, restTemplateBuilderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      restTemplateBuilderMock,
      restTemplateMock
    );
  }

  @Test
  void whenGetIngestionFlowFileControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> {
        processExecutionsApisHolder.getIngestionFlowFileControllerApi(accessToken)
          .createIngestionFlowFile(new IngestionFlowFileRequestDTO());
        return voidMock;
      },
      new ParameterizedTypeReference<>() {},
      processExecutionsApisHolder::unload);
  }

  @Test
  void whenGetIngestionFlowFileEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken ->
        processExecutionsApisHolder.getIngestionFlowFileEntityControllerApi(accessToken)
          .crudGetIngestionflowfile("123"),
      new ParameterizedTypeReference<>() {},
      processExecutionsApisHolder::unload);
  }

  @Test
  void whenGetIngestionFlowFileEntityExtendedControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken ->
        processExecutionsApisHolder.getIngestionFlowFileEntityExtendedControllerApi(accessToken)
          .updateStatus(1L, IngestionFlowFileStatus.ERROR, IngestionFlowFileStatus.ERROR, 0L, 0L, "1.0","", null),
      new ParameterizedTypeReference<>() {},
      processExecutionsApisHolder::unload);
  }

  @Test
  void whenGetIngestionFlowFileSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken ->
        processExecutionsApisHolder.getIngestionFlowFileSearchControllerApi(accessToken)
          .crudIngestionFlowFilesFindByOrganizationIdAndFilePathNameAndFileName(1L, "filePath", "fileName"),
      new ParameterizedTypeReference<>() {},
      processExecutionsApisHolder::unload);
  }

  @Test
  void whenGetExportFileEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> processExecutionsApisHolder.getExportFileEntityControllerApi(accessToken)
          .crudGetExportfile("123"),
      new ParameterizedTypeReference<>() {},
      processExecutionsApisHolder::unload);
  }

}
