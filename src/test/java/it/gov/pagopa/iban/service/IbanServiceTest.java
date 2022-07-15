package it.gov.pagopa.iban.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.dto.AccountCheckIbanDTO;
import it.gov.pagopa.iban.dto.AccountHolderCheckIbanDTO;
import it.gov.pagopa.iban.dto.BankInfoCheckIbanDTO;
import it.gov.pagopa.iban.dto.CheckIbanDTO;
import it.gov.pagopa.iban.dto.ErrorCheckIbanDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.dto.PayloadCheckIbanDTO;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = {IbanService.class})
class IbanServiceTest {

  @Autowired
  IbanService ibanService;

  @MockBean
  IbanRepository ibanRepository;
  @MockBean
  CheckIbanRestConnector checkIbanRestConnector;


  private static final String USER_ID = "test_user_test";
  private static final String USER_ID_UNKNOW = "test_user_test_unknow_checkiban";
  private static final String IBAN_OK = "IT09P3608105138205493205495";
  private static final String BIC_CODE = "ACCHLDMMXXX";
  private static final String CHECK_IBAN_STATUS = "OK";
  private static final String HOLDER_BANK = "ACCOUNT HOLDER BANK";
  private static final LocalDateTime QUEUE_DATE = LocalDateTime.now();
  private static final String VALIDATION_STATUS = "OK";
  private static final String VALUE = "IT12A1234512345123456789012";
  private static final String VALUE_TYPE = "IBAN";
  private static final String TYPE = "PERSON_NATURAL";
  private static final String FISCAL_CODE = "ABCDEF12L12A123K";
  private static final String VAT_CODE = null;
  private static final String TAX_CODE = null;
  private static final String BUSINESS_NAME = "ACCOUNT HOLDER BANK";
  private static final String CITY = "ROMA";
  private static final String COUNTRY_CODE = "IT";
  private static final String BRANCH_NAME = "HEAD OFFICE";


  private static final List<ErrorCheckIbanDTO> ERRORS = new ArrayList<>();

  private static final AccountCheckIbanDTO ACCOUNT_CHECK_IBAN_DTO = new AccountCheckIbanDTO(VALUE,
      VALUE_TYPE, BIC_CODE);
  private static final AccountHolderCheckIbanDTO ACCOUNT_HOLDER_CHECK_IBAN_DTO = new AccountHolderCheckIbanDTO(
      TYPE, FISCAL_CODE, VAT_CODE, TAX_CODE);
  private static final BankInfoCheckIbanDTO BANK_INFO_CHECK_IBAN_DTO = new BankInfoCheckIbanDTO(
      BUSINESS_NAME, CITY, COUNTRY_CODE, BIC_CODE, BRANCH_NAME);
  private static final PayloadCheckIbanDTO PAYLOAD = new PayloadCheckIbanDTO(VALIDATION_STATUS,
      ACCOUNT_CHECK_IBAN_DTO, ACCOUNT_HOLDER_CHECK_IBAN_DTO, BANK_INFO_CHECK_IBAN_DTO);
  private static final IbanModel IBAN = new IbanModel(USER_ID, IBAN_OK, BIC_CODE, CHECK_IBAN_STATUS,
      HOLDER_BANK, QUEUE_DATE);
  private static final CheckIbanDTO CHECK_IBAN_DTO = new CheckIbanDTO(VALIDATION_STATUS, ERRORS,
      PAYLOAD);
  private static final IbanQueueDTO IBAN_QUEUE_DTO = new IbanQueueDTO(USER_ID, IBAN_OK,
      LocalDateTime.now().toString());


  @Test
  void getIbanList_ok() {
    List<IbanModel> ibanModelList = new ArrayList<>();
    ibanModelList.add(IBAN);
    Mockito.when(ibanRepository.findByUserId(USER_ID)).thenReturn(ibanModelList);

    List<IbanDTO> ibanDTOList = ibanService.getIbanList(USER_ID);

    assertFalse(ibanDTOList.isEmpty());

    IbanDTO actual = ibanDTOList.get(0);
    assertEquals(IBAN.getIban(), actual.getIban());
    assertEquals(IBAN.getCheckIbanStatus(), actual.getCheckIbanStatus());
    assertEquals(IBAN.getHolderBank(), actual.getHolderBank());

  }

  @Test
  void getIbanList_ok_empty() {
    List<IbanModel> ibanModelList = new ArrayList<>();
    Mockito.when(ibanRepository.findByUserId(USER_ID)).thenReturn(ibanModelList);

    List<IbanDTO> ibanDTOList = ibanService.getIbanList(USER_ID);

    assertTrue(ibanDTOList.isEmpty());
  }

  @Test
  void saveIban_ok() {
    Mockito.when(checkIbanRestConnector.checkIban(IBAN_OK, USER_ID))
        .thenReturn(CHECK_IBAN_DTO);
    IbanModel ibanModel = new IbanModel();

    Mockito.doAnswer(invocationOnMock -> {
      ibanModel.setUserId(USER_ID);
      ibanModel.setIban(IBAN_OK);
      ibanModel.setCheckIbanStatus(CHECK_IBAN_DTO.getStatus());
      ibanModel.setBicCode(CHECK_IBAN_DTO.getPayload().getBankInfo().getBicCode());
      ibanModel.setHolderBank(CHECK_IBAN_DTO.getPayload().getBankInfo().getBusinessName());
      ibanModel.setQueueDate(LocalDateTime.parse(IBAN_QUEUE_DTO.getQueueDate()));

      return null;
    }).when(ibanRepository).save(Mockito.any(IbanModel.class));

    ibanService.saveIban(IBAN_QUEUE_DTO);
    assertEquals(ibanModel.getUserId(), IBAN_QUEUE_DTO.getUserId());
    assertEquals(ibanModel.getIban(), IBAN_QUEUE_DTO.getIban());
  }

}
