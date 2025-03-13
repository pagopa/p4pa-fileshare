package it.gov.pagopa.pu.fileshare.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

  @Test
  void givenValidFileWhenCalculateFileHashThenVerifyHash()
    throws NoSuchAlgorithmException, IOException {
    // Given
    File tempFile = File.createTempFile("testFile", ".txt");
    tempFile.deleteOnExit();

    try (FileWriter writer = new FileWriter(tempFile)) {
      writer.write("TEST FILE HASH P4PA SEND");
    }

    String expectedHash = "9e9LsYp4qQ4bjyGI4Mp/jmBN2jKehKTTaonMr1AJEPU=";
    // When
    String actualHash = FileUtils.calculateFileHash(tempFile);

    // Then
    assertEquals(expectedHash, actualHash);
  }

  @Test
  void givenNotExistsFileWhenCalculateFileHashThenException() {
    // Given
    File nonExistentFile = new File("FILENOTFOUND.txt");

    // Then
    assertThrows(IOException.class, () -> FileUtils.calculateFileHash(nonExistentFile));
  }
}
