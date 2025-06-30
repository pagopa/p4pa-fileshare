package it.gov.pagopa.pu.fileshare.connector.debtpositions.client;

import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptNoPiiEntityControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;
import it.gov.pagopa.pu.fileshare.connector.debtpositions.config.DebtPositionsApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class ReceiptClientTest {
  @Mock
  private DebtPositionsApisHolder debtPositionsApisHolderMock;
  @Mock
  private ReceiptNoPiiEntityControllerApi receiptNoPiiEntityControllerApiMock;

  private ReceiptClient client;

  @BeforeEach
  void setUp() {
    client = new ReceiptClient(debtPositionsApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      debtPositionsApisHolderMock,
      receiptNoPiiEntityControllerApiMock
    );
  }

  @Test
  void whenGetReceiptByIdThenInvokeWithAccessToken() {
    // Given
    long receiptId = 1L;
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII expectedResult = new ReceiptNoPII();

    Mockito.when(debtPositionsApisHolderMock.getReceiptNoPiiEntityControllerApi(accessToken))
      .thenReturn(receiptNoPiiEntityControllerApiMock);
    Mockito.when(receiptNoPiiEntityControllerApiMock.crudGetReceiptnopii(String.valueOf(receiptId)))
      .thenReturn(expectedResult);

    // When
    ReceiptNoPII result = client.getReceiptById(receiptId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenNoExistentReceiptIdWhenGetReceiptByIdThenNull() {
    // Given
    long receiptId = 1L;
    String accessToken = "ACCESSTOKEN";

    Mockito.when(debtPositionsApisHolderMock.getReceiptNoPiiEntityControllerApi(accessToken))
      .thenReturn(receiptNoPiiEntityControllerApiMock);
    Mockito.when(receiptNoPiiEntityControllerApiMock.crudGetReceiptnopii(String.valueOf(receiptId)))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    ReceiptNoPII result = client.getReceiptById(receiptId, accessToken);

    // Then
    Assertions.assertNull(result);
  }

}
