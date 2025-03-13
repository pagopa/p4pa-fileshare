package it.gov.pagopa.pu.fileshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileUtils {

  private FileUtils() {
  }

  public static String calculateFileHash(File file)
    throws NoSuchAlgorithmException, IOException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try(DigestInputStream digestInputStream = new DigestInputStream(new FileInputStream(file), digest)){
      byte[] inputStreamBuffer = new byte[8192];
      while (digestInputStream.read(inputStreamBuffer) > -1);
    }
    byte[] hash = digest.digest();
    return Base64.getEncoder().encodeToString(hash);
  }
}

