package it.gov.pagopa.iban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.iban.dto.IbanDTO;
import it.gov.pagopa.iban.service.IbanService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = {IbanController.class})
class IbanControllerTest {

  @Autowired
  IbanController ibanController;

  @MockBean
  IbanService ibanService;

  @Autowired
  protected MockMvc mvc;

  @Autowired
  ObjectMapper objectMapper;

  private static final String USER_ID = "TEST_USER_ID";
  private static final String IBAN_OK = "IT09P3608105138205493205495";
  private static final String HOLDER_BANK= "ACCOUNT HOLDER BANK";
  private static final String CHECK_IBAN_STATUS= "OK";

  private static final String BASE_URL = "/idpay/iban";




  private static final IbanDTO IBAN_DTO = new IbanDTO(IBAN_OK,CHECK_IBAN_STATUS,HOLDER_BANK);

  @Test
  void getIbanList_ok() throws Exception {
    List<IbanDTO> ibanDTOList = new ArrayList<>();
    ibanDTOList.add(IBAN_DTO);
    Mockito.when(ibanService.getIbanList(USER_ID)).thenReturn(ibanDTOList);

    MvcResult res = mvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/" + USER_ID )
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

  }
}
