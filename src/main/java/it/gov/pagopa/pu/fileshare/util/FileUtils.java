package it.gov.pagopa.pu.fileshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileUtils {

  private FileUtils() {
  }

  public static String calculateFileHash(File file)
    throws NoSuchAlgorithmException, IOException {
    try (InputStream inputStream = new FileInputStream(file)) {
      return calculateHash(inputStream);
    }
  }

  public static String calculateFileHash(InputStream inputStream)
    throws NoSuchAlgorithmException, IOException {
    return calculateHash(inputStream);
  }

  private static String calculateHash(InputStream inputStream)
    throws NoSuchAlgorithmException, IOException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
      byte[] inputStreamBuffer = new byte[8192];
      while (digestInputStream.read(inputStreamBuffer) > -1);
    }
    byte[] hash = digest.digest();
    return Base64.getEncoder().encodeToString(hash);
  }
}

