package it.gov.pagopa.iban.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.FeignException;
import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.decrypt.DecryptRestConnector;
import it.gov.pagopa.iban.dto.AccountCheckIbanDTO;
import it.gov.pagopa.iban.dto.AccountHolderCheckIbanDTO;
import it.gov.pagopa.iban.dto.BankInfoCheckIbanDTO;
import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import it.gov.pagopa.iban.dto.ErrorCheckIbanDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.dto.PayloadCheckIbanDTO;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = IbanServiceImpl.class)
class IbanServiceTest {

    @MockBean
    IbanRepository ibanRepositoryMock;
    @Autowired
    IbanService ibanService;
    @MockBean
    CheckIbanRestConnector checkIbanRestConnector;
    @MockBean
    DecryptRestConnector decryptRestConnector;

    private static final String USER_ID = "TRNFNC96R02H501I";
    private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
    private static final String INITIATIVE_ID_OK = "123";
    private static final String IBAN_OK = "IT43O0326822300052755845000";
    private static final String IBAN_KO_NOT_IT = "GB29NWBK60161331926819";
    private static final String IBAN_WRONG = "it99C1234567890123456789012222";
    private static final String IBAN_WRONG_2 = "IT09P3608105138205493205496";
    private static final String DESCRIPTION_OK = "conto cointestato";
    private static final String CHANNEL_OK = "APP-IO";
    private static final String CHECK_IBAN_STATUS= "OK";
    private static final String HOLDER_BANK_OK = "BANCA SELLA SPA";
    private static final String BIC_CODE= "SELBIT2B";
    private static final String VALIDATION_STATUS = "OK";
    private static final IbanModel IBAN_MODEL = new IbanModel(USER_ID,IBAN_OK,CHECK_IBAN_STATUS,BIC_CODE,HOLDER_BANK_OK,LocalDateTime.now());
    private static final IbanQueueDTO IBAN_QUEUE_DTO = new IbanQueueDTO(USER_ID,IBAN_OK,LocalDateTime.now().toString());
    private static final IbanQueueDTO IBAN_QUEUE_DTO_KO = new IbanQueueDTO(USER_ID,IBAN_WRONG,LocalDateTime.now().toString());

    private static final IbanModel IBAN_MODEL_EMPTY = new IbanModel();
    private static final AccountCheckIbanDTO ACCOUNT_CHECK_IBAN_DTO = new AccountCheckIbanDTO(IBAN_OK,"IBAN",BIC_CODE);
    private static final AccountHolderCheckIbanDTO ACCOUNT_HOLDER_CHECK_IBAN_DTO = new AccountHolderCheckIbanDTO("PERSON_NATURAL",USER_ID,"","");
    private static final BankInfoCheckIbanDTO BANK_INFO_CHECK_IBAN_DTO = new BankInfoCheckIbanDTO("ACCOUNT HOLDER BANK", "ROMA","IT","SELBIT2B","HEAD OFFICE");
    private static final DecryptedCfDTO DECRYPTED_CF_DTO = new DecryptedCfDTO("63adb280-39d1-4c59-962f-a88eacd19ec8");
    private static final String CHECK_IBAN_STATUS_UNKNOWN= "UNKNOWN_PSP";
    private static final PayloadCheckIbanDTO PAYLOAD_DTO = new PayloadCheckIbanDTO(VALIDATION_STATUS,ACCOUNT_CHECK_IBAN_DTO,ACCOUNT_HOLDER_CHECK_IBAN_DTO,BANK_INFO_CHECK_IBAN_DTO);
    private static final ErrorCheckIbanDTO ERROR_DTO_1 = new ErrorCheckIbanDTO("PGPA-0008","Invalid IBAN code","account.value");
    private static final ErrorCheckIbanDTO ERROR_DTO_2 = new ErrorCheckIbanDTO("PGPA-0018","Invalid credentials provided","");
    private static final ErrorCheckIbanDTO ERROR_DTO_3 = new ErrorCheckIbanDTO("REQ005","Missing request header: apikey","");
    private static final ErrorCheckIbanDTO ERROR_DTO_4 = new ErrorCheckIbanDTO("PGPA-0004","No available API found for this URI",null);
    private static final List<ErrorCheckIbanDTO> ERROR_LIST = new ArrayList<>();


    @Test
    void getIbanList_ok(){
        List<IbanModel> ibanModelList = new ArrayList<>();
        ibanModelList.add(IBAN_MODEL);
        Mockito.when(ibanRepositoryMock.findByUserId(USER_ID)).thenReturn(ibanModelList);
        List<IbanDTO> ibanDTO = ibanService.getIbanList(USER_ID);

        IbanDTO actual = ibanDTO.get(0);
        assertEquals(IBAN_MODEL.getIban(), actual.getIban());
        assertEquals(IBAN_MODEL.getCheckIbanStatus(), actual.getCheckIbanStatus());
        assertEquals(IBAN_MODEL.getHolderBank(), actual.getHolderBank());
    }

    @Test
    void getIbanList_empty(){
        List<IbanModel> ibanModelList = new ArrayList<>();
        Mockito.when(ibanRepositoryMock.findByUserId(USER_ID)).thenReturn(ibanModelList);
        List<IbanDTO> ibanDTO = ibanService.getIbanList(USER_ID);
        assertTrue(ibanDTO.isEmpty());
    }

    @Test
    void save_iban_ok(){
        ResponseCheckIbanDTO response = new ResponseCheckIbanDTO(CHECK_IBAN_STATUS,ERROR_LIST,PAYLOAD_DTO);
        Mockito.when(decryptRestConnector.getPiiByToken(IBAN_QUEUE_DTO.getUserId())).thenReturn(DECRYPTED_CF_DTO);
        Mockito.when(checkIbanRestConnector.checkIban(IBAN_QUEUE_DTO.getIban(),DECRYPTED_CF_DTO.getPii())).thenReturn(response);
        Mockito.doAnswer(invocationOnMock -> {
            IBAN_MODEL_EMPTY.setUserId(IBAN_QUEUE_DTO.getUserId());
            IBAN_MODEL_EMPTY.setIban(IBAN_QUEUE_DTO.getIban());
            IBAN_MODEL_EMPTY.setQueueDate(LocalDateTime.parse(IBAN_QUEUE_DTO.getQueueDate()));
            IBAN_MODEL_EMPTY.setCheckIbanStatus("OK");
            return null;
        }).when(ibanRepositoryMock).save(Mockito.any(IbanModel.class));

        ibanService.saveIban(IBAN_QUEUE_DTO);

        assertEquals(IBAN_MODEL_EMPTY.getIban(), IBAN_QUEUE_DTO.getIban());
        assertEquals(IBAN_MODEL_EMPTY.getUserId(), IBAN_QUEUE_DTO.getUserId());
        assertEquals(IBAN_MODEL_EMPTY.getQueueDate().toString(), IBAN_QUEUE_DTO.getQueueDate());
        assertEquals(response.getStatus(), IBAN_MODEL_EMPTY.getCheckIbanStatus());

    }

    @Test
    void save_iban_ko(){
        ErrorCheckIbanDTO errorCheckIbanDTO = new ErrorCheckIbanDTO("PGPA-0017","PSP Not Present in Routing Subsystem",null);
        ERROR_LIST.add(errorCheckIbanDTO);
        PayloadCheckIbanDTO payload = new PayloadCheckIbanDTO("KO",ACCOUNT_CHECK_IBAN_DTO,ACCOUNT_HOLDER_CHECK_IBAN_DTO,BANK_INFO_CHECK_IBAN_DTO);

        ResponseCheckIbanDTO response = new ResponseCheckIbanDTO("UNKNOWN_PSP",ERROR_LIST,PAYLOAD_DTO);

        Mockito.when(decryptRestConnector.getPiiByToken(IBAN_QUEUE_DTO.getUserId())).thenReturn(DECRYPTED_CF_DTO);
        Mockito.when(checkIbanRestConnector.checkIban(IBAN_WRONG,DECRYPTED_CF_DTO.getPii())).thenReturn(response);
        Mockito.doAnswer(invocationOnMock -> {
            IBAN_MODEL_EMPTY.setUserId(IBAN_QUEUE_DTO.getUserId());
            IBAN_MODEL_EMPTY.setIban(IBAN_QUEUE_DTO.getIban());
            IBAN_MODEL_EMPTY.setQueueDate(LocalDateTime.parse(IBAN_QUEUE_DTO.getQueueDate()));
            IBAN_MODEL_EMPTY.setCheckIbanStatus("UNKNOWN_PSP");
            IBAN_MODEL_EMPTY.setErrorCode(errorCheckIbanDTO.getCode());
            IBAN_MODEL_EMPTY.setCheckIbanResponseDate(LocalDateTime.now());
            IBAN_MODEL_EMPTY.setErrorDescription(errorCheckIbanDTO.getDescription());
            return null;
        }).when(ibanRepositoryMock).save(Mockito.any(IbanModel.class));

        try {
            ibanService.saveIban(IBAN_QUEUE_DTO_KO);
        }catch (FeignException e){
            assertFalse(e.getMessage().isEmpty());
        }
        assertEquals(response.getStatus(), IBAN_MODEL_EMPTY.getCheckIbanStatus());


    }

//    @Test
//    void putIban_idemp(){
//        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_OK, DESCRIPTION_OK);
//        iban.setEnabled(true);
//        iban.setInsertIbanTimestamp(LocalDateTime.now());
//
//        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
//                .thenReturn(
//                        List.of(iban));
//        try {
//            ibanService.putIban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_OK,DESCRIPTION_OK);
//        } catch (IbanException e){
//            Assertions.fail();
//        }
//
//    }
//
//    @Test
//    void putIban_ko_iban_not_italian() {
//        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_KO_NOT_IT, DESCRIPTION_OK);
//        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
//                .thenReturn(
//                        Collections.emptyList());
//        try {
//            ibanService.putIban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_KO_NOT_IT, DESCRIPTION_OK);
//            Assertions.fail();
//        } catch (UnsupportedCountryException e) {
//            assertNotNull(e.getMessage());
//        }
//
//    }
//
//    @Test
//    void putIban_ko_iban_wrong() {
//        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG, DESCRIPTION_OK);
//        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
//                .thenReturn(
//                        Collections.emptyList());
//        try {
//            ibanService.putIban(INITIATIVE_ID, USER_ID_OK, IBAN_WRONG, DESCRIPTION_OK);
//            Assertions.fail();
//
//        } catch (IbanFormatException e) {
//            assertNotNull(e.getMessage());
//        }
//
//    }
//
//    @Test
//    void putIban_ko_iban_digit_controll() {
//        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG_2, DESCRIPTION_OK);
//        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
//                .thenReturn(
//                        Collections.emptyList());
//        try {
//            ibanService.putIban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG_2, DESCRIPTION_OK);
//            Assertions.fail();
//        } catch (InvalidCheckDigitException e) {
//            assertNotNull(e.getMessage());
//        }
//
//    }
//
//    @Test
//    void getIban_ok() {
//        final IbanModel iban = new IbanModel(USER_ID_OK,INITIATIVE_ID, IBAN_OK, DESCRIPTION_OK);
//        iban.setEnabled(true);
//        iban.setInsertIbanTimestamp(LocalDateTime.now());
//
//        IbanDTO ibanDTO = new IbanDTO(IBAN_OK,DESCRIPTION_OK, HOLDER_BANK_OK,CHANNEL_OK);
//
//        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserIdAndEnabledTrue(INITIATIVE_ID_OK, USER_ID_OK))
//                .thenReturn(Optional.of(iban));
//        ibanService.getIban(INITIATIVE_ID_OK, USER_ID_OK);
//
//        assertEquals(ibanDTO.getIban(), iban.getIbanCode());
//
//    }
//
//    @Test
//    void getIban_ko() {
//        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserIdAndEnabledTrue(INITIATIVE_ID, USER_ID))
//                .thenReturn(Optional.empty());
//        try {
//            ibanService.getIban(INITIATIVE_ID, USER_ID);
//        } catch (IbanException e) {
//            assertEquals(HttpStatus.NOT_FOUND.value(), e.getCode());
//        }
//
//    }
}
