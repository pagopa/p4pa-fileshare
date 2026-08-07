package it.gov.pagopa.pu.fileshare.exception;

import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO;
import it.gov.pagopa.pu.fileshare.dto.generated.FileshareErrorDTO.CategoryEnum;
import it.gov.pagopa.pu.fileshare.exception.common.CommonExceptionHandlerTest;
import it.gov.pagopa.pu.fileshare.exception.custom.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class FileshareExceptionHandlerTest extends CommonExceptionHandlerTest {

  @Test
  void handleOrganizationMissMatchException() throws Exception {
    doThrow(new OrganizationMissMatchException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(CategoryEnum.NOT_FOUND.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist());
  }

  @Test
  void handleInvalidFileException() throws Exception {
    doThrow(new InvalidFileException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(FileshareErrorDTO.CategoryEnum.INVALID_FILE.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist());
  }

  @Test
  void handleInvalidFileTypeException() throws Exception {
    doThrow(new InvalidFileTypeException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isBadRequest())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(CategoryEnum.INVALID_FILE_TYPE.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleFlowFileNotFoundException() throws Exception {
    doThrow(new FileNotFoundException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(CategoryEnum.NOT_FOUND.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleUnauthorizedFileDownloadException() throws Exception {
    doThrow(new UnauthorizedFileDownloadException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isUnauthorized())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(CategoryEnum.UNAUTHORIZED.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleFileUploadException() throws Exception {
    doThrow(new FileUploadException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isInternalServerError())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(FileshareErrorDTO.CategoryEnum.FILE_UPLOAD_ERROR.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleFileNotFoundException() throws Exception {
    doThrow(new java.io.FileNotFoundException("File not found"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("NOT_FOUND"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("File not found"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleFileAlreadyExistsException() throws Exception {
    doThrow(new java.nio.file.FileAlreadyExistsException("Conflict"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Conflict"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleCustomFileAlreadyExistsException() throws Exception {
    doThrow(new FileAlreadyExistsException("CODE", "Conflict"))
      .when(requestMappingHandlerAdapterSpy).handle(any(), any(), any());

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value("CONFLICT"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Conflict"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleIngestionFlowFileNotFoundException() throws Exception {
    doThrow(new IngestionFlowFileNotFoundException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(CategoryEnum.NOT_FOUND.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

  @Test
  void handleReceiptNotFoundException() throws Exception {
    doThrow(new ReceiptNotFoundException("CODE", "Error")).when(testControllerSpy).testEndpoint(DATA, BODY);

    performRequest(DATA, MediaType.APPLICATION_JSON)
      .andExpect(MockMvcResultMatchers.status().isNotFound())
      .andExpect(MockMvcResultMatchers.jsonPath("$.category").value(CategoryEnum.NOT_FOUND.toString()))
      .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("CODE"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Error"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.fields").doesNotExist())
      .andExpect(MockMvcResultMatchers.jsonPath("$.traceId").value(traceId));
  }

}
