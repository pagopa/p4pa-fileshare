package it.gov.pagopa.pu.fileshare.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

  @Test
  void givenValidInputStreamWhenCalculateFileHashThenVerifyHash()
    throws NoSuchAlgorithmException, IOException {
    // Given
    String content = "TEST FILE HASH P4PA SEND";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(
      StandardCharsets.UTF_8));

    byte[] expectedHash = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));

    //String expectedHash = "9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=";
    // When
    byte[] actualHash = FileUtils.calculateHash(inputStream);

    // Then
    assertEquals(expectedHash, actualHash);
  }

  @Test
  void givenValidInputStreamWhenCalculateFileBase64HashThenVerifyHash()
    throws IOException, NoSuchAlgorithmException {
    // Given: Input byte array for a known hash
    String content = "TEST FILE HASH P4PA SEND";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(
      StandardCharsets.UTF_8));

    // When: calculateBase64FileHash is called
    byte[] hash = FileUtils.calculateHash(inputStream);
    String base64Hash = FileUtils.calculateBase64FileHash(hash);

    // Then: The result should match the expected Base64 value
    assertEquals(Base64.getEncoder().encodeToString(hash), base64Hash,
      "The Base64 result should match the expected encoding");
  }

}
