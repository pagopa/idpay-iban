package it.gov.pagopa.iban.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.gov.pagopa.iban.constants.IbanConstants;
import it.gov.pagopa.iban.dto.ErrorDTO;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanPutDTO;
import it.gov.pagopa.iban.exception.IbanException;
import it.gov.pagopa.iban.model.IbanModel;
import it.gov.pagopa.iban.service.IbanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = {
        IbanController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class IbanControllerTest {

    @MockBean
    IbanService ibanServiceMock;
    @Autowired
    protected MockMvc mvc;

    private static final Logger LOG = LoggerFactory.getLogger(
            IbanModel.class);

    private static final String BASE_URL = "http://localhost:8080/idpay/iban";
    private static final String ENROLLMENT_IBAN_URL = "/enroll";
    private static final String USER_ID_OK = "123";
    private static final String INITIATIVE_ID_OK = "123";
    private static final String IBAN_OK = "it99C1234567890123456789012";
    private static final String IBAN_WRONG = "it99C1234567890123456789012222";
    private static final String CHANNEL_OK = "APP-IO";
    private static final String DESCRIPTION_OK = "conto cointestato";
    private static final String HOLDER_BANK_OK = "Unicredit";

    private static final IbanPutDTO IBAN_BODY_DTO_EMPTY = new IbanPutDTO("","","","");

    @Test
    void putIban_ok() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Mockito.doNothing().when(ibanServiceMock).putIban(INITIATIVE_ID_OK,USER_ID_OK,IBAN_OK,DESCRIPTION_OK);
        IbanPutDTO ibanPutDTO = new IbanPutDTO(USER_ID_OK, INITIATIVE_ID_OK, IBAN_OK, DESCRIPTION_OK);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.put(BASE_URL + ENROLLMENT_IBAN_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(ibanPutDTO)).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();
    }

    @Test
    void putIban_ko() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Mockito.doThrow(new IbanException(HttpStatus.BAD_REQUEST.value(),"" )).when(ibanServiceMock).putIban(INITIATIVE_ID_OK,USER_ID_OK,IBAN_WRONG,DESCRIPTION_OK);
        IbanPutDTO ibanPutDTO = new IbanPutDTO(USER_ID_OK, INITIATIVE_ID_OK, IBAN_WRONG, DESCRIPTION_OK);
        MvcResult res = mvc.perform(MockMvcRequestBuilders.put(BASE_URL + ENROLLMENT_IBAN_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(ibanPutDTO)).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        ErrorDTO error = objectMapper.readValue(res.getResponse().getContentAsString(), ErrorDTO.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getCode());
    }

    @Test
    void enroll_empty_body() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        MvcResult res = mvc.perform(MockMvcRequestBuilders.put(BASE_URL + ENROLLMENT_IBAN_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(IBAN_BODY_DTO_EMPTY))
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();

        ErrorDTO error = objectMapper.readValue(res.getResponse().getContentAsString(), ErrorDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getCode());
        assertTrue(error.getMessage().contains(IbanConstants.ERROR_MANDATORY_FIELD));
    }

    @Test
    void getIban_ok() throws Exception {
        IbanDTO ibanDTO = new IbanDTO(IBAN_OK,DESCRIPTION_OK,HOLDER_BANK_OK, CHANNEL_OK);

        Mockito.when(ibanServiceMock.getCurrentIban(INITIATIVE_ID_OK, USER_ID_OK))
            .thenReturn(ibanDTO);

        mvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + "/" + INITIATIVE_ID_OK + "/" + USER_ID_OK)
                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    void getOnboardingStatus_ko() throws Exception {

        Mockito.doThrow(new IbanException(HttpStatus.NOT_FOUND.value(),
                String.format("Iban with initiativeId %s and userId %s not found.", INITIATIVE_ID_OK,
                    USER_ID_OK)))
            .when(ibanServiceMock).getCurrentIban(INITIATIVE_ID_OK, USER_ID_OK);

        mvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + "/" + INITIATIVE_ID_OK + "/" + USER_ID_OK)
                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();
    }


}
