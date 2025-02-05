package it.gov.pagopa.pu.fileshare.config.json;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

class OffsetDateTimeToLocalDateTimeDeserializerTest {

  private final OffsetDateTimeToLocalDateTimeDeserializer deserializer = new OffsetDateTimeToLocalDateTimeDeserializer();

  @Test
  void givenOffsetDateTimeWhenThenOk() throws IOException {
    // Given
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString())
      .thenReturn(offsetDateTime.toString());

    // When
    LocalDateTime result = deserializer.deserialize(parser, null);

    // Then
    Assertions.assertEquals(offsetDateTime.toLocalDateTime(), result);
  }

  @Test
  void givenLocalDateTimeWhenThenOk() throws IOException {
    // Given
    LocalDateTime localDateTime = LocalDateTime.now();
    JsonParser parser = Mockito.mock(JsonParser.class);
    Mockito.when(parser.getValueAsString())
      .thenReturn(localDateTime.toString());

    // When
    LocalDateTime result = deserializer.deserialize(parser, null);

    // Then
    Assertions.assertEquals(localDateTime, result);
  }
}
