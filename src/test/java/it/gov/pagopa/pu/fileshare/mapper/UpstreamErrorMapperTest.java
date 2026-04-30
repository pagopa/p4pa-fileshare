package it.gov.pagopa.pu.fileshare.mapper;

import it.gov.pagopa.pu.fileshare.dto.UpstreamErrorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpstreamErrorMapperTest {

  @Mock
  private JsonMapper jsonMapper;

  private UpstreamErrorMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new UpstreamErrorMapper(jsonMapper);
  }

  @Test
  void givenParsableBodyWithBracketCodeWhenFromThenExtractCodeAndDescription() {
    // given
    String body = """
      {"code":"UPSTREAM_CODE","message":"[INVALID_IBAN] eltjhreigjpo","traceId":"t1"}
      """;

    HttpClientErrorException ex = HttpClientErrorException.create(
      HttpStatus.BAD_REQUEST,
      "Bad Request",
      HttpHeaders.EMPTY,
      body.getBytes(StandardCharsets.UTF_8),
      StandardCharsets.UTF_8
    );

    UpstreamErrorDTO dto = new UpstreamErrorDTO();
    dto.setMessage("[INVALID_IBAN] eltjhreigjpo");

    when(jsonMapper.readValue(body, UpstreamErrorDTO.class))
      .thenReturn(dto);

    // when
    UpstreamErrorMapper.MappedUpstreamError mapped = mapper.from(ex);

    // then
    assertNotNull(mapped);
    assertEquals("INVALID_IBAN", mapped.code());
    assertEquals("[INVALID_IBAN] eltjhreigjpo", mapped.description());

    verify(jsonMapper).readValue(body, UpstreamErrorDTO.class);
    verifyNoMoreInteractions(jsonMapper);
  }

  @Test
  void givenParsableBodyWithoutBracketCodeWhenFromThenGenericCodeAndDescriptionIsMessage() {
    // given
    String body = """
      {"code":"UPSTREAM_CODE","message":"Just a plain message","traceId":"t1"}
      """;

    HttpClientErrorException ex = HttpClientErrorException.create(
      HttpStatus.BAD_REQUEST,
      "Bad Request",
      HttpHeaders.EMPTY,
      body.getBytes(StandardCharsets.UTF_8),
      StandardCharsets.UTF_8
    );

    UpstreamErrorDTO dto = new UpstreamErrorDTO();
    dto.setMessage("Just a plain message");

    when(jsonMapper.readValue(body, UpstreamErrorDTO.class))
      .thenReturn(dto);

    // when
    UpstreamErrorMapper.MappedUpstreamError mapped = mapper.from(ex);

    // then
    assertNotNull(mapped);
    assertEquals("GENERIC_ERROR", mapped.code());
    assertEquals("Just a plain message", mapped.description());

    verify(jsonMapper).readValue(body, UpstreamErrorDTO.class);
    verifyNoMoreInteractions(jsonMapper);
  }

  @Test
  void givenEmptyBodyWhenFromThenFallbackToExceptionMessageAndNoJsonRead() {
    // given
    HttpClientErrorException ex = HttpClientErrorException.create(
      HttpStatus.BAD_REQUEST,
      "Error",
      HttpHeaders.EMPTY,
      new byte[0],
      StandardCharsets.UTF_8
    );

    // when
    UpstreamErrorMapper.MappedUpstreamError mapped = mapper.from(ex);

    // then
    assertNotNull(mapped);
    assertEquals("GENERIC_ERROR", mapped.code());
    assertEquals(ex.getMessage(), mapped.description());

    verifyNoInteractions(jsonMapper);
  }

  @Test
  void givenJsonParsingFailsWhenFromThenFallbackToExceptionMessage() {
    // given
    String body = """
      {"code":"UPSTREAM_CODE","message":"[INVALID_IBAN] eltjhreigjpo","traceId":"t1"}
      """;

    HttpClientErrorException ex = HttpClientErrorException.create(
      HttpStatus.BAD_REQUEST,
      "Error",
      HttpHeaders.EMPTY,
      body.getBytes(StandardCharsets.UTF_8),
      StandardCharsets.UTF_8
    );

    when(jsonMapper.readValue(body, UpstreamErrorDTO.class))
      .thenThrow(new RuntimeException("boom"));

    // when
    UpstreamErrorMapper.MappedUpstreamError mapped = mapper.from(ex);

    // then
    assertNotNull(mapped);
    assertEquals("GENERIC_ERROR", mapped.code());
    assertEquals(ex.getMessage(), mapped.description());

    verify(jsonMapper).readValue(body, UpstreamErrorDTO.class);
    verifyNoMoreInteractions(jsonMapper);
  }

  @Test
  void givenUpstreamMessageIsBlankWhenFromThenUseFallbackMessage() {
    // given
    String body = """
      {"code":"UPSTREAM_CODE","message":"   ","traceId":"t1"}
      """;

    HttpClientErrorException ex = HttpClientErrorException.create(
      HttpStatus.BAD_REQUEST,
      "Fallback error message",
      HttpHeaders.EMPTY,
      body.getBytes(StandardCharsets.UTF_8),
      StandardCharsets.UTF_8
    );

    UpstreamErrorDTO dto = new UpstreamErrorDTO();
    dto.setMessage("   ");

    when(jsonMapper.readValue(body, UpstreamErrorDTO.class))
      .thenReturn(dto);

    // when
    UpstreamErrorMapper.MappedUpstreamError mapped = mapper.from(ex);

    // then
    assertNotNull(mapped);
    assertEquals("GENERIC_ERROR", mapped.code());
    assertEquals(ex.getMessage(), mapped.description());

    verify(jsonMapper).readValue(body, UpstreamErrorDTO.class);
    verifyNoMoreInteractions(jsonMapper);
  }
}
