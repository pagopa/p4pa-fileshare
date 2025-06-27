package it.gov.pagopa.pu.fileshare.service.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;
import it.gov.pagopa.pu.fileshare.connector.debtpositions.ReceiptService;
import it.gov.pagopa.pu.fileshare.dto.FileResourceDTO;
import it.gov.pagopa.pu.fileshare.exception.custom.FileNotFoundException;
import it.gov.pagopa.pu.fileshare.exception.custom.OrganizationMissMatchException;
import it.gov.pagopa.pu.fileshare.exception.custom.ReceiptNotFoundException;
import it.gov.pagopa.pu.fileshare.service.FileStorerService;
import it.gov.pagopa.pu.fileshare.util.TestUtils;
import it.gov.pagopa.pu.p4paauth.dto.generated.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class ReceiptRtRetrieverServiceTest {

  @Mock
  private ReceiptService receiptServiceMock;
  @Mock
  private FileStorerService fileStorerServiceMock;

  private ReceiptRtRetrieverService service;

  @BeforeEach
  void init(){
    this.service = new ReceiptRtRetrieverServiceImpl(receiptServiceMock, fileStorerServiceMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      receiptServiceMock,
      fileStorerServiceMock
    );
  }

  @Test
  void givenNoAdminWhenDownloadRtThenThrowAuthorizationDeniedException(){
    // Given
    long organizationId = 1L;
    long receiptId = 2L;
    UserInfo user = TestUtils.getSampleUser();
    String accessToken = "ACCESSTOKEN";

    // When, Then
    Assertions.assertThrows(AuthorizationDeniedException.class, () -> service.downloadRt(organizationId, receiptId, user, accessToken));
  }

  @Test
  void givenNoReceiptWhenDownloadRtThenThrowReceiptNotFoundException(){
    // Given
    long organizationId = 1L;
    long receiptId = 2L;
    UserInfo user = TestUtils.getSampleAdminUser();
    String accessToken = "ACCESSTOKEN";

    Mockito.when(receiptServiceMock.getReceiptById(receiptId, accessToken))
      .thenReturn(null);

    // When, Then
    Assertions.assertThrows(ReceiptNotFoundException.class, () -> service.downloadRt(organizationId, receiptId, user, accessToken));
  }

  @Test
  void givenMissMatchedOrganizationIdWhenDownloadRtThenThrowOrganizationMissMatchException(){
    // Given
    long organizationId = 1L;
    long receiptId = 2L;
    UserInfo user = TestUtils.getSampleAdminUser();
    String accessToken = "ACCESSTOKEN";

    ReceiptNoPII receipt = new ReceiptNoPII();
    receipt.setOrgFiscalCode("OTHERORG");

    Mockito.when(receiptServiceMock.getReceiptById(receiptId, accessToken))
      .thenReturn(receipt);

    // When, Then
    Assertions.assertThrows(OrganizationMissMatchException.class, () -> service.downloadRt(organizationId, receiptId, user, accessToken));
  }

  @Test
  void givenReceiptWithoutRtWhenDownloadRtThenThrowFileNotFoundException(){
    // Given
    long organizationId = 1L;
    long receiptId = 2L;
    UserInfo user = TestUtils.getSampleAdminUser();
    String accessToken = "ACCESSTOKEN";

    ReceiptNoPII receipt = new ReceiptNoPII();
    receipt.setOrgFiscalCode("ORG_FC1");

    Mockito.when(receiptServiceMock.getReceiptById(receiptId, accessToken))
      .thenReturn(receipt);

    // When, Then
    Assertions.assertThrows(FileNotFoundException.class, () -> service.downloadRt(organizationId, receiptId, user, accessToken));
  }

  @Test
  void givenCompleteReceiptWhenDownloadRtThenThrowFileNotFoundException() throws IOException {
    // Given
    long organizationId = 1L;
    long receiptId = 2L;
    UserInfo user = TestUtils.getSampleAdminUser();
    String accessToken = "ACCESSTOKEN";

    String filePath = "filePath";
    String fileName = "rt.xml";
    ReceiptNoPII receipt = new ReceiptNoPII();
    receipt.setOrgFiscalCode("ORG_FC1");
    receipt.setRtFilePath(filePath+"/"+fileName);

    Path orgDir = Path.of("/orgDir");
    InputStream fileStream = Mockito.mock(InputStream.class);

    Mockito.when(receiptServiceMock.getReceiptById(receiptId, accessToken))
      .thenReturn(receipt);
    Mockito.when(fileStorerServiceMock.buildOrganizationBasePath(organizationId))
        .thenReturn(orgDir);
    Mockito.when(fileStorerServiceMock.decryptFile(orgDir.resolve(filePath), fileName))
      .thenReturn(fileStream);

    // When
    FileResourceDTO result = service.downloadRt(organizationId, receiptId, user, accessToken);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(fileStream, result.getResourceStream().getInputStream());
    Assertions.assertEquals(fileName, result.getFileName());
  }
}
