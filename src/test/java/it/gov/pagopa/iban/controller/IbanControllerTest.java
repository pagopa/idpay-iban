package it.gov.pagopa.iban.controller;

import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.service.IbanService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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

    private static final String BASE_URL = "http://localhost:8080/idpay/iban";
    private static final String USER_ID_OK = "123";
    private static final String IBAN_OK = "it99C1234567890123456789012";
    private static final String CHECK_IBAN_STATUS= "UNKNOWN_PSP";
    private static final String HOLDER_BANK_OK = "Unicredit";
    private static final IbanDTO IBAN_DTO = new IbanDTO(IBAN_OK,CHECK_IBAN_STATUS,HOLDER_BANK_OK);


    @Test
    void getIban_ok() throws Exception {
        List<IbanDTO> ibanDTOList = new ArrayList<>();
        ibanDTOList.add(IBAN_DTO);
        Mockito.when(ibanServiceMock.getIbanList(USER_ID_OK))
            .thenReturn(ibanDTOList);

        mvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + "/" + USER_ID_OK)
                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Test
    void getIban_empty() throws Exception {
        List<IbanDTO> ibanDTOList = new ArrayList<>();
        Mockito.when(ibanServiceMock.getIbanList(USER_ID_OK))
            .thenReturn(ibanDTOList);

        mvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + "/" + USER_ID_OK)
                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }
}
