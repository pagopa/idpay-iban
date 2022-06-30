package it.gov.pagopa.iban.service;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.Iban;
import it.gov.pagopa.iban.repository.IbanRepository;
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

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private static final String IBAN_OK = "it99C1234567890123456789012";
    private static final String IBAN_KO_NOT_IT = "en99C1234567890123456789012";
    private static final String IBAN_WRONG = "it99C1234567890123456789012222";
    private static final String IBAN_WRONG_2 = "itX9C1234567890123456789012";
    private static final String DESCRIPTION_OK = "conto cointestato";
    private static final String CHANNEL_OK = "APP-IO";
    private static final String HOLDER_BANK_OK = "Unicredit";

    @Test
    void putIban_ok(){

        final Iban iban = new Iban(USER_ID_OK,INITIATIVE_ID_OK,IBAN_OK,DESCRIPTION_OK);

        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID, USER_ID))
                .thenReturn(Collections.emptyList());

        Mockito.doAnswer(invocationOnMock -> {
            iban.setEnabled(true);
            iban.setInsertIbanTimestamp(LocalDateTime.now());
            return null;
        }).when(ibanRepositoryMock).save(Mockito.any(Iban.class));
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
        final Iban iban = new Iban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_OK, DESCRIPTION_OK);
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
        final Iban iban = new Iban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_KO_NOT_IT, DESCRIPTION_OK);
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        Collections.emptyList());
        try {
            ibanService.putIban(iban.getInitiativeId(), iban.getUserId(), iban.getIbanCode(), iban.getDescription());
        } catch (IbanException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getCode());
        }

    }

    @Test
    void putIban_ko_iban_wrong() {
        final Iban iban = new Iban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG, DESCRIPTION_OK);
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        Collections.emptyList());
        try {
            ibanService.putIban(iban.getInitiativeId(), iban.getUserId(), iban.getIbanCode(), iban.getDescription());
        } catch (IbanException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getCode());
        }

    }

    @Test
    void putIban_ko_iban_wrong2() {
        final Iban iban = new Iban(INITIATIVE_ID_OK, USER_ID_OK, IBAN_WRONG_2, DESCRIPTION_OK);
        Mockito.when(ibanRepositoryMock.findByInitiativeIdAndUserId(INITIATIVE_ID_OK, USER_ID_OK))
                .thenReturn(
                        Collections.emptyList());
        try {
            ibanService.putIban(iban.getInitiativeId(), iban.getUserId(), iban.getIbanCode(), iban.getDescription());
        } catch (IbanException e) {
            assertEquals(HttpStatus.BAD_REQUEST.value(), e.getCode());
        }

    }

    @Test
    void getIban_ok() {
        final Iban iban = new Iban(USER_ID_OK,INITIATIVE_ID, IBAN_OK, DESCRIPTION_OK);
        iban.setEnabled(true);
        iban.setInsertIbanTimestamp(LocalDateTime.now());

        IbanDTO ibanDTO = new IbanDTO(IBAN_OK,DESCRIPTION_OK,CHANNEL_OK);

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
