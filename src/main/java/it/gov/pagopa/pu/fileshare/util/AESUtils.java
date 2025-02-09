package it.gov.pagopa.pu.fileshare.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class AESUtils {
  private AESUtils() {
  }

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;
  private static final int SALT_LENGTH_BYTE = 16;
  private static final String ALGORITHM_TYPE = "AES";
  private static final int KEY_LENGTH = 256;
  private static final int ITERATION_COUNT = 65536;
  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  public static final String CIPHER_EXTENSION = ".cipher";

  private static byte[] getRandomNonce(int length) {
    byte[] nonce = new byte[length];
    new SecureRandom().nextBytes(nonce);
    return nonce;
  }

  private static SecretKey getSecretKey(String password, byte[] salt) {
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);

    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);
      return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Cannot initialize cryptographic data", e);
    }
  }

  /** It will encrypt the input message using the provided password */
  public static byte[] encrypt(String password, String plainMessage) {
    byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
    SecretKey secretKey = getSecretKey(password, salt);

    // GCM recommends 12 bytes iv
    byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
    Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

    byte[] encryptedMessageByte = executeCipherOp(cipher, plainMessage.getBytes(UTF_8));

    // prefix IV and Salt to cipher text
    return ByteBuffer.allocate(iv.length + salt.length + encryptedMessageByte.length)
      .put(iv)
      .put(salt)
      .put(encryptedMessageByte)
      .array();
  }

  /** It will wrap the provided inputStream into a ciphered inputStream using the provided password */
  public static InputStream encrypt(String password, InputStream plainStream) {
    byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
    SecretKey secretKey = getSecretKey(password, salt);

    // GCM recommends 12 bytes iv
    byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
    Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

    // prefix IV and Salt to cipher text
    byte[] prefix = ByteBuffer.allocate(iv.length + salt.length)
      .put(iv)
      .put(salt)
      .array();

    return new SequenceInputStream(
      new ByteArrayInputStream(prefix),
      new CipherInputStream(new BufferedInputStream(plainStream), cipher));
  }

  /** It will read and store the provided inputStream as a ciphered file using the provided password.<BR />
   * If the ciphered file already exists, it will throw {@link java.nio.file.FileAlreadyExistsException} */
  public static void encryptAndSave(String password, InputStream plainStream, Path targetPath, String fileName)
    throws IOException {
    Path targetCipherFile = targetPath.resolve(fileName + CIPHER_EXTENSION);
    try (InputStream is = plainStream;
         InputStream cipherIs = encrypt(password, is)) {
      Files.copy(cipherIs, targetCipherFile);
    }
  }

  /** It will cipher the provided file using the provided password.<BR />
   * If the ciphered file already exists, it will throw {@link java.nio.file.FileAlreadyExistsException} */
  public static File encrypt(String password, File plainFile) {
    File cipherFile = new File(plainFile.getAbsolutePath() + CIPHER_EXTENSION);
    try (FileInputStream fis = new FileInputStream(plainFile);
         InputStream cipherStream = encrypt(password, fis)) {
      Files.copy(cipherStream, cipherFile.toPath());
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong when ciphering input file " + plainFile.getAbsolutePath(), e);
    }
    return cipherFile;
  }

  /** It will decrypt the provided cipher message using the provided password */
  public static String decrypt(String password, byte[] cipherMessage) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);

    byte[] iv = new byte[IV_LENGTH_BYTE];
    byteBuffer.get(iv);

    byte[] salt = new byte[SALT_LENGTH_BYTE];
    byteBuffer.get(salt);

    byte[] encryptedByte = new byte[byteBuffer.remaining()];
    byteBuffer.get(encryptedByte);

    SecretKey secretKey = getSecretKey(password, salt);
    Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secretKey, iv);

    byte[] decryptedMessageByte = executeCipherOp(cipher, encryptedByte);
    return new String(decryptedMessageByte, UTF_8);
  }

  /** It will wrap the provided inputStream into a decrypted inputStream using the provided password */
  public static InputStream decrypt(String password, InputStream cipherStream) {
    try {
      byte[] iv = cipherStream.readNBytes(IV_LENGTH_BYTE);
      byte[] salt = cipherStream.readNBytes(SALT_LENGTH_BYTE);

      SecretKey secretKey = getSecretKey(password, salt);
      Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secretKey, iv);

      return new CipherInputStream(new BufferedInputStream(cipherStream), cipher);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read AES prefix data", e);
    }
  }

  /** @see #decrypt(String, Path, String, File)  */
  public static void decrypt(String password, File cipherFile, File outputPlainFile){
    AESUtils.decrypt(password, cipherFile.getParentFile().toPath(), cipherFile.getName(), outputPlainFile);
  }

  /** It will decrypt the provided file using the provided password.<BR />
   * If the decrypted file already exists, it will override it */
  public static void decrypt(String password, Path filePath, String fileName, File outputPlainFile) {
    Path cipherFilePath = resolveCipherFilePath(filePath, fileName);

    try (FileInputStream fis = new FileInputStream(cipherFilePath.toFile());
         InputStream plainStream = decrypt(password, fis)) {
      Files.copy(plainStream, outputPlainFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Something went wrong when deciphering input file " + cipherFilePath, e);
    }
  }

  /** @see #decrypt(String, Path, String, File)  */
  public static InputStream decrypt(String password, File cipherFile){
    return AESUtils.decrypt(password, cipherFile.getParentFile().toPath(), cipherFile.getName());
  }

  /** It will create an InputStream to read the provided file decrypting it using the given password */
  public static InputStream decrypt(String password, Path filePath, String fileName) {
    Path cipherFilePath = resolveCipherFilePath(filePath, fileName);
    try {
      return decrypt(password, new FileInputStream(cipherFilePath.toFile()));
    } catch (FileNotFoundException e) {
      throw new IllegalStateException("Something went wrong when deciphering input file " + cipherFilePath, e);
    }
  }

  private static Path resolveCipherFilePath(Path filePath, String fileName) {
    Path cipherFilePath;
    if(fileName.endsWith(CIPHER_EXTENSION)) {
      cipherFilePath = filePath.resolve(fileName);
    } else {
      cipherFilePath = filePath.resolve(fileName + CIPHER_EXTENSION);
    }
    return cipherFilePath;
  }

  private static byte[] executeCipherOp(Cipher cipher, byte[] encryptedByte) {
    try {
      return cipher.doFinal(encryptedByte);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new IllegalStateException("Cannot execute cipher op", e);
    }
  }

  private static Cipher initCipher(int mode, SecretKey secretKey, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
      return cipher;
    } catch (NoSuchPaddingException | NoSuchAlgorithmException |
             InvalidKeyException
             | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException("Cannot initialize cipher data", e);
    }
  }

}
