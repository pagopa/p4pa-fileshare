package it.gov.pagopa.pu.fileshare.mapper;

import it.gov.pagopa.pu.fileshare.dto.UpstreamErrorDTO;
import it.gov.pagopa.pu.fileshare.util.ErrorMessageParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class UpstreamErrorMapper {
  private final JsonMapper jsonMapper;

  public UpstreamErrorMapper(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public record MappedUpstreamError(String code, String description) {
  }

  public MappedUpstreamError from(HttpClientErrorException ex) {
    UpstreamErrorDTO upstream = tryParse(ex);

    String fallbackMessage = ex.getMessage();
    String upstreamMessage = upstream != null ? upstream.getMessage() : null;

    String resolvedMessage = StringUtils.firstNonBlank(upstreamMessage, fallbackMessage);

    ErrorMessageParser.ParsedError parsed = ErrorMessageParser.parse(resolvedMessage);

    return new MappedUpstreamError(parsed.code(), resolvedMessage);
  }

  private UpstreamErrorDTO tryParse(HttpClientErrorException ex) {
    try {
      String body = ex.getResponseBodyAsString();
      if (StringUtils.isBlank(body)) {
        return null;
      }
      return jsonMapper.readValue(body, UpstreamErrorDTO.class);
    } catch (Exception e) {
      return null;
    }
  }
}
