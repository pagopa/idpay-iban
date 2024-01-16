package it.gov.pagopa.iban.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.common.web.exception.ServiceExceptionHandler;
import it.gov.pagopa.common.web.exception.ValidationExceptionHandler;
import it.gov.pagopa.iban.config.ServiceExceptionConfig;
import it.gov.pagopa.iban.constants.IbanConstants;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.dto.IbanListDTO;
import it.gov.pagopa.iban.exception.IbanNotFoundException;
import it.gov.pagopa.iban.service.IbanService;
import java.time.LocalDateTime;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = {
    IbanController.class, ServiceExceptionConfig.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class IbanControllerTest {

  @MockBean
  IbanService ibanServiceMock;
  @Autowired
  protected MockMvc mvc;
  @Autowired
  ObjectMapper objectMapper;

  private static final String BASE_URL = "http://localhost:8080/idpay/iban";
  private static final String USER_ID = "123";
  private static final String IBAN = "it99C1234567890123456789012";
  private static final String IBAN_KO = "iban_ko";
  private static final String CHECK_IBAN_STATUS = "UNKNOWN_PSP";
  private static final String HOLDER_BANK_OK = "Unicredit";
  private static final String CHANNEL = "APP_IO";
  private static final String DESCRIPTION = "conto intestato";
  private static final IbanDTO IBAN_DTO = new IbanDTO(IBAN, CHECK_IBAN_STATUS,DESCRIPTION,CHANNEL, HOLDER_BANK_OK,
          LocalDateTime.now());


  @Test
  void getIbanList_ok() throws Exception {
    IbanListDTO ibanList = new IbanListDTO();
    List<IbanDTO> ibanDTOList = new ArrayList<>();
    ibanDTOList.add(IBAN_DTO);
    ibanList.setIbanList(ibanDTOList);
    Mockito.when(ibanServiceMock.getIbanList(USER_ID))
        .thenReturn(ibanList);

    mvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
  }

  @Test
  void getIbanList_empty() throws Exception {
    IbanListDTO ibanList = new IbanListDTO();
    List<IbanDTO> ibanDTOList = new ArrayList<>();
    ibanList.setIbanList(ibanDTOList);
    Mockito.when(ibanServiceMock.getIbanList(USER_ID))
        .thenReturn(ibanList);

    mvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
  }

  @Test
  void getIban_ok() throws Exception {
    Mockito.when(ibanServiceMock.getIban(IBAN, USER_ID))
        .thenReturn(IBAN_DTO);

    mvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/" + IBAN + "/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
  }

  @Test
  void getIban_ko() throws Exception {
    Mockito.doThrow(
            new IbanNotFoundException(
                    IbanConstants.ExceptionCode.IBAN_NOT_FOUND, String.format("Iban for userId %s not found.",
                USER_ID)))
        .when(ibanServiceMock)
        .getIban(IBAN, USER_ID);

    MvcResult res =
        mvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + "/" + IBAN + "/"+ USER_ID)
                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound()).andReturn();

    ErrorDTO error =
        objectMapper.readValue(res.getResponse().getContentAsString(), ErrorDTO.class);
    assertEquals(IbanConstants.ExceptionCode.IBAN_NOT_FOUND, error.getCode());
    assertEquals(String.format("Iban for userId %s not found.",
        USER_ID), error.getMessage());
  }
}
