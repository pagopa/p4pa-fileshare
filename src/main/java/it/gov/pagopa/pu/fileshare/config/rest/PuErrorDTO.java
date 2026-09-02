package it.gov.pagopa.pu.fileshare.config.rest;

import it.gov.pagopa.pu.fileshare.dto.generated.ErrorFieldDTO;

import java.util.List;

public record PuErrorDTO(
  String category,
  String code,
  String message,
  List<ErrorFieldDTO> fields
) {
}
