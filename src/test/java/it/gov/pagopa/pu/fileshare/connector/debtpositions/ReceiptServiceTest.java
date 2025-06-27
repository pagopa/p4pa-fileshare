package it.gov.pagopa.pu.fileshare.connector.debtpositions;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptNoPII;
import it.gov.pagopa.pu.fileshare.connector.debtpositions.client.ReceiptClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

  @Mock
  private ReceiptClient clientMock;

  private ReceiptService service;

  @BeforeEach
  void init(){
    service = new ReceiptServiceImpl(clientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(clientMock);
  }

  @Test
  void whenGetReceiptByIdThenInvokeClient(){
    // Given
    Long receiptId = 1L;
    String accessToken = "ACCESSTOKEN";
    ReceiptNoPII expectedResult = new ReceiptNoPII();

    Mockito.when(clientMock.getReceiptById(Mockito.same(receiptId), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    ReceiptNoPII result = service.getReceiptById(receiptId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
