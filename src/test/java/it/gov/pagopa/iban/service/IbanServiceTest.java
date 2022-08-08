package it.gov.pagopa.iban.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mongodb.assertions.Assertions;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import it.gov.pagopa.iban.checkiban.CheckIbanRestConnector;
import it.gov.pagopa.iban.decrypt.DecryptRestConnector;
import it.gov.pagopa.iban.dto.AccountCheckIbanDTO;
import it.gov.pagopa.iban.dto.AccountHolderCheckIbanDTO;
import it.gov.pagopa.iban.dto.BankInfoCheckIbanDTO;
import it.gov.pagopa.iban.dto.DecryptedCfDTO;
import it.gov.pagopa.iban.dto.ErrorCheckIbanDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanListDTO;
import it.gov.pagopa.iban.dto.IbanQueueDTO;
import it.gov.pagopa.iban.dto.PayloadCheckIbanDTO;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import it.gov.pagopa.iban.event.IbanProducer;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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

    @MockBean
    IbanProducer ibanProducer;

    private static final String USER_ID = "TRNFNC96R02H501I";
    private static final String IBAN_OK = "IT43O0326822300052755845000";
    private static final String IBAN_WRONG = "it99C1234567890123456789012222";
    private static final String CHECK_IBAN_STATUS= "OK";
    private static final String HOLDER_BANK_OK = "BANCA SELLA SPA";
    private static final String BIC_CODE= "SELBIT2B";
    private static final String VALIDATION_STATUS = "OK";
    private static final String CHANNEL= "APP_IO";
    private static final String DESCRIPTION= "conto intestato";
    private static final IbanModel IBAN_MODEL = new IbanModel(USER_ID,IBAN_OK,CHECK_IBAN_STATUS,BIC_CODE,HOLDER_BANK_OK,LocalDateTime.now());
    private static final IbanQueueDTO IBAN_QUEUE_DTO = new IbanQueueDTO(USER_ID,IBAN_OK,CHANNEL,DESCRIPTION,LocalDateTime.now().toString());
    private static final IbanQueueDTO IBAN_QUEUE_DTO_KO = new IbanQueueDTO(USER_ID,IBAN_WRONG,CHANNEL,DESCRIPTION,LocalDateTime.now().toString());
    private static final IbanModel IBAN_MODEL_EMPTY = new IbanModel();
    private static final AccountCheckIbanDTO ACCOUNT_CHECK_IBAN_DTO = new AccountCheckIbanDTO(IBAN_OK,"IBAN",BIC_CODE);
    private static final AccountHolderCheckIbanDTO ACCOUNT_HOLDER_CHECK_IBAN_DTO = new AccountHolderCheckIbanDTO("PERSON_NATURAL",USER_ID,"","");
    private static final BankInfoCheckIbanDTO BANK_INFO_CHECK_IBAN_DTO = new BankInfoCheckIbanDTO("ACCOUNT HOLDER BANK", "ROMA","IT","SELBIT2B","HEAD OFFICE");
    private static final DecryptedCfDTO DECRYPTED_CF_DTO = new DecryptedCfDTO("63adb280-39d1-4c59-962f-a88eacd19ec8");
    private static final PayloadCheckIbanDTO PAYLOAD_DTO = new PayloadCheckIbanDTO(VALIDATION_STATUS,ACCOUNT_CHECK_IBAN_DTO,ACCOUNT_HOLDER_CHECK_IBAN_DTO,BANK_INFO_CHECK_IBAN_DTO);
    private static final List<ErrorCheckIbanDTO> ERROR_LIST = new ArrayList<>();


    @Test
    void getIbanList_ok(){
        List<IbanModel> ibanModelList = new ArrayList<>();
        ibanModelList.add(IBAN_MODEL);
        Mockito.when(ibanRepositoryMock.findByUserId(USER_ID)).thenReturn(ibanModelList);
        IbanListDTO ibanDTO = ibanService.getIbanList(USER_ID);

        IbanDTO actual = ibanDTO.getIbanList().get(0);
        assertEquals(IBAN_MODEL.getIban(), actual.getIban());
        assertEquals(IBAN_MODEL.getCheckIbanStatus(), actual.getCheckIbanStatus());
        assertEquals(IBAN_MODEL.getHolderBank(), actual.getHolderBank());
    }

    @Test
    void getIbanList_empty(){
        List<IbanModel> ibanModelList = new ArrayList<>();
        Mockito.when(ibanRepositoryMock.findByUserId(USER_ID)).thenReturn(ibanModelList);
        try {
            ibanService.getIbanList(USER_ID);
            Assertions.fail();
        } catch (IbanException e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getCode());
        }
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
    void save_iban_unknown(){
        ErrorCheckIbanDTO errorCheckIbanDTO = new ErrorCheckIbanDTO("PGPA-0010","Error from PSP",null);
        ERROR_LIST.add(errorCheckIbanDTO);
        PayloadCheckIbanDTO payload = new PayloadCheckIbanDTO("KO",ACCOUNT_CHECK_IBAN_DTO,ACCOUNT_HOLDER_CHECK_IBAN_DTO,BANK_INFO_CHECK_IBAN_DTO);

        ResponseCheckIbanDTO response = new ResponseCheckIbanDTO("UNKNOWN_PSP",ERROR_LIST,payload);

        Mockito.when(decryptRestConnector.getPiiByToken(IBAN_QUEUE_DTO.getUserId())).thenReturn(DECRYPTED_CF_DTO);
        Request request =
            Request.create(
                Request.HttpMethod.POST, "url", new HashMap<>(), null, new RequestTemplate());
        Mockito.doThrow(new FeignException.BadGateway("", request, new byte[0], null))
            .when(checkIbanRestConnector).checkIban(IBAN_OK,DECRYPTED_CF_DTO.getPii());
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
        } catch (FeignException e) {
            assertEquals(HttpStatus.BAD_GATEWAY.value(), e.status());
        }
        assertEquals(response.getStatus(), IBAN_MODEL_EMPTY.getCheckIbanStatus());
//        assertEquals(response.getErrors().get(0).getDescription(), errorCheckIbanDTO.getDescription());
//        assertEquals(response.getErrors().get(0).getCode(), errorCheckIbanDTO.getCode());
    }

    @Test
    void save_iban_ko(){
        ErrorCheckIbanDTO errorCheckIbanDTO = new ErrorCheckIbanDTO("PGPA-0017","PSP Not Present in Routing Subsystem",null);
        ERROR_LIST.add(errorCheckIbanDTO);
        PayloadCheckIbanDTO payload = new PayloadCheckIbanDTO("KO",ACCOUNT_CHECK_IBAN_DTO,ACCOUNT_HOLDER_CHECK_IBAN_DTO,BANK_INFO_CHECK_IBAN_DTO);

        ResponseCheckIbanDTO response = new ResponseCheckIbanDTO("UNKNOWN_PSP",ERROR_LIST,payload);

        Mockito.when(decryptRestConnector.getPiiByToken(IBAN_QUEUE_DTO.getUserId())).thenReturn(DECRYPTED_CF_DTO);
        Request request =
            Request.create(
                Request.HttpMethod.POST, "url", new HashMap<>(), null, new RequestTemplate());
        Mockito.doThrow(new FeignException.NotImplemented("", request, new byte[0], null))
            .when(checkIbanRestConnector).checkIban(IBAN_WRONG,DECRYPTED_CF_DTO.getPii());
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
        } catch (FeignException e) {
            assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), e.status());
        }
        assertEquals(response.getStatus(), IBAN_MODEL_EMPTY.getCheckIbanStatus());
//        assertEquals(response.getErrors().get(0).getDescription(), errorCheckIbanDTO.getDescription());
//        assertEquals(response.getErrors().get(0).getCode(), errorCheckIbanDTO.getCode());

    }
    @Test
    void save_iban_ko_checkiban(){
        ErrorCheckIbanDTO errorCheckIbanDTO = new ErrorCheckIbanDTO("PGPA-0008","Invalid IBAN code",null);
        ERROR_LIST.add(errorCheckIbanDTO);
        Mockito.when(decryptRestConnector.getPiiByToken(IBAN_QUEUE_DTO.getUserId())).thenReturn(DECRYPTED_CF_DTO);
        Request request =
            Request.create(
                Request.HttpMethod.POST, "url", new HashMap<>(), null, new RequestTemplate());
        Mockito.doThrow(new FeignException.BadRequest("", request, new byte[0], null))
            .when(checkIbanRestConnector).checkIban(IBAN_WRONG,DECRYPTED_CF_DTO.getPii());

        Mockito.doAnswer(invocationOnMock -> {
            IBAN_MODEL_EMPTY.setUserId(IBAN_QUEUE_DTO.getUserId());
            IBAN_MODEL_EMPTY.setIban(IBAN_QUEUE_DTO.getIban());
            IBAN_MODEL_EMPTY.setQueueDate(LocalDateTime.parse(IBAN_QUEUE_DTO.getQueueDate()));
            IBAN_MODEL_EMPTY.setCheckIbanStatus("KO");
            IBAN_MODEL_EMPTY.setErrorCode(errorCheckIbanDTO.getCode());
            IBAN_MODEL_EMPTY.setCheckIbanResponseDate(LocalDateTime.now());
            IBAN_MODEL_EMPTY.setErrorDescription(errorCheckIbanDTO.getDescription());
            return null;
        }).when(ibanRepositoryMock).save(Mockito.any(IbanModel.class));

        try {
            ibanService.saveIban(IBAN_QUEUE_DTO_KO);
        } catch (FeignException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.status());
        }
    }

    @Test
    void save_iban_ko_decrypt(){
        ErrorCheckIbanDTO errorCheckIbanDTO = new ErrorCheckIbanDTO("PGPA-0004","No available API found for this URI",null);
        ERROR_LIST.add(errorCheckIbanDTO);
        PayloadCheckIbanDTO payload = new PayloadCheckIbanDTO("KO",ACCOUNT_CHECK_IBAN_DTO,ACCOUNT_HOLDER_CHECK_IBAN_DTO,BANK_INFO_CHECK_IBAN_DTO);

        ResponseCheckIbanDTO response = new ResponseCheckIbanDTO("KO",ERROR_LIST,payload);

        Request request =
            Request.create(
                Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        Mockito.doThrow(new FeignException.BadRequest("", request, new byte[0], null))
            .when(decryptRestConnector).getPiiByToken(IBAN_QUEUE_DTO.getUserId());

        Mockito.when(checkIbanRestConnector.checkIban(IBAN_WRONG,DECRYPTED_CF_DTO.getPii())).thenReturn(response);

        Mockito.doAnswer(invocationOnMock -> {
            IBAN_MODEL_EMPTY.setUserId(IBAN_QUEUE_DTO.getUserId());
            IBAN_MODEL_EMPTY.setIban(IBAN_QUEUE_DTO.getIban());
            IBAN_MODEL_EMPTY.setQueueDate(LocalDateTime.parse(IBAN_QUEUE_DTO.getQueueDate()));
            IBAN_MODEL_EMPTY.setCheckIbanStatus("KO");
            IBAN_MODEL_EMPTY.setErrorCode(errorCheckIbanDTO.getCode());
            IBAN_MODEL_EMPTY.setCheckIbanResponseDate(LocalDateTime.now());
            IBAN_MODEL_EMPTY.setErrorDescription(errorCheckIbanDTO.getDescription());
            return null;
        }).when(ibanRepositoryMock).save(Mockito.any(IbanModel.class));

        try {
            ibanService.saveIban(IBAN_QUEUE_DTO_KO);
        } catch (FeignException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.status());
        }
    }

    @Test
    void getIban_ok() {
        Mockito.when(ibanRepositoryMock.findByIbanAndUserId(IBAN_OK, USER_ID))
            .thenReturn(Optional.of(IBAN_MODEL));
        IbanDTO ibanDTO = ibanService.getIban(IBAN_OK, USER_ID);

        assertEquals(ibanDTO.getIban(), IBAN_MODEL.getIban());
        assertEquals(ibanDTO.getCheckIbanStatus(), IBAN_MODEL.getCheckIbanStatus());
        assertEquals(ibanDTO.getDescription(), IBAN_MODEL.getDescription());
        assertEquals(ibanDTO.getHolderBank(), IBAN_MODEL.getHolderBank());
        assertEquals(ibanDTO.getChannel(), IBAN_MODEL.getChannel());
    }

    @Test
    void getIban_ko() {
        Mockito.when(ibanRepositoryMock.findByIbanAndUserId(IBAN_OK, USER_ID))
            .thenReturn(Optional.empty());
        try {
            ibanService.getIban(IBAN_OK, USER_ID);
            Assertions.fail();
        } catch (IbanException e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getCode());
        }
    }
}
