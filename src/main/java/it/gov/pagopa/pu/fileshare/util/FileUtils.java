package it.gov.pagopa.pu.fileshare.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileUtils {

  private FileUtils() {
  }

  public static String calculateBase64FileHash(byte[] hash) {
    return Base64.getEncoder().encodeToString(hash);
  }

  public static byte[] calculateHash(InputStream inputStream)
    throws IOException, NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
      byte[] inputStreamBuffer = new byte[8192];
      while (digestInputStream.read(inputStreamBuffer) > -1);
    }
    return digest.digest();
  }
}

