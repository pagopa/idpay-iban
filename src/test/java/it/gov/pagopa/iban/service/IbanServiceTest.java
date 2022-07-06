package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.repository.IbanRepository;
import org.iban4j.CountryCode;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = {
        IbanService.class})
class IbanServiceTest {

    @MockBean
    IbanRepository ibanRepositoryMock;

    @Autowired
    IbanService ibanService;

    private static final String USER_ID = "TEST_USER_ID";
    private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
    private static final String USER_ID_OK = "123";
    private static final String INITIATIVE_ID_OK = "123";
    private static final String IBAN_OK = "IT09P3608105138205493205495";
    private static final String IBAN_KO_NOT_IT = "GB29NWBK60161331926819";
    private static final String IBAN_WRONG = "it99C1234567890123456789012222";
    private static final String IBAN_WRONG_2 = "IT09P3608105138205493205496";
    private static final String DESCRIPTION_OK = "conto cointestato";
    private static final String CHANNEL_OK = "APP-IO";
    private static final String HOLDER_BANK_OK = "Unicredit";

    @Test
    void putIban_ok(){

        final IbanModel iban = new IbanModel(USER_ID_OK,INITIATIVE_ID_OK,IBAN_OK,DESCRIPTION_OK);

        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID, USER_ID))
                .thenReturn(Collections.emptyList());

        Mockito.doAnswer(invocationOnMock -> {
            iban.setEnabled(true);
            iban.setInsertIbanTimestamp(LocalDateTime.now());
            return null;
        }).when(ibanRepositoryMock).save(Mockito.any(IbanModel.class));
        ibanService.putIban(iban.getInitiativeId(),iban.getUserId(), iban.getIbanCode(), iban.getDescription());

        assertEquals(INITIATIVE_ID_OK, iban.getInitiativeId());
        assertEquals(USER_ID_OK, iban.getUserId());
        assertEquals(CHANNEL_OK, iban.getChannel());
        assertEquals(IBAN_OK, iban.getIbanCode());
        assertEquals(HOLDER_BANK_OK, iban.getHolderBank());
        assertEquals(DESCRIPTION_OK, iban.getDescription());
        assertTrue(iban.isEnabled());
    }

    @Test
    void putIban_idemp(){
        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_OK, DESCRIPTION_OK);
        iban.setEnabled(true);
        iban.setInsertIbanTimestamp(LocalDateTime.now());

        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        List.of(iban));
        try {
            ibanService.putIban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_OK,DESCRIPTION_OK);
        } catch (IbanException e){
            Assertions.fail();
        }

    }

    @Test
    void putIban_ko_iban_not_italian() {
        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_KO_NOT_IT, DESCRIPTION_OK);
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        Collections.emptyList());
        try {
            ibanService.putIban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_KO_NOT_IT, DESCRIPTION_OK);
            Assertions.fail();
        } catch (UnsupportedCountryException e) {
            assertNotNull(e.getMessage());
        }

    }

    @Test
    void putIban_ko_iban_wrong() {
        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG, DESCRIPTION_OK);
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        Collections.emptyList());
        try {
            ibanService.putIban(INITIATIVE_ID, USER_ID_OK, IBAN_WRONG, DESCRIPTION_OK);
            Assertions.fail();

        } catch (IbanFormatException e) {
            assertNotNull(e.getMessage());
        }

    }

    @Test
    void putIban_ko_iban_digit_controll() {
        final IbanModel iban = new IbanModel(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG_2, DESCRIPTION_OK);
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        Collections.emptyList());
        try {
            ibanService.putIban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG_2, DESCRIPTION_OK);
            Assertions.fail();
        } catch (InvalidCheckDigitException e) {
            assertNotNull(e.getMessage());
        }

    }

    @Test
    void getIban_ok() {
        final IbanModel iban = new IbanModel(USER_ID_OK,INITIATIVE_ID, IBAN_OK, DESCRIPTION_OK);
        iban.setEnabled(true);
        iban.setInsertIbanTimestamp(LocalDateTime.now());

        IbanDTO ibanDTO = new IbanDTO(IBAN_OK,DESCRIPTION_OK, HOLDER_BANK_OK,CHANNEL_OK);

        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserIdAndEnabledTrue(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(Optional.of(iban));
        ibanService.getIban(INITIATIVE_ID_OK, USER_ID_OK);

        assertEquals(ibanDTO.getIban(), iban.getIbanCode());

    }

    @Test
    void getIban_ko() {
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserIdAndEnabledTrue(INITIATIVE_ID, USER_ID))
                .thenReturn(Optional.empty());
        try {
            ibanService.getIban(INITIATIVE_ID, USER_ID);
        } catch (IbanException e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getCode());
        }

    }
}
