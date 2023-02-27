package it.gov.pagopa.iban.checkiban;

import it.gov.pagopa.iban.dto.RequestCheckIbanDTO;
import it.gov.pagopa.iban.dto.ResponseCheckIbanDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class CheckIbanRestConnectorImpl implements CheckIbanRestConnector {
  private final String apikey;
  private final String authSchema;
  private final CheckIbanRestClient checkIbanRestClient;
  private static final String IBAN="IBAN";
  private static final String PERSON_NATURAL="PERSON_NATURAL";

  public CheckIbanRestConnectorImpl(
      @Value("${rest-client.checkiban.apikey}") String apikey,
      @Value("${rest-client.checkiban.authSchema}") String authSchema,
      CheckIbanRestClient checkIbanRestClient) {
    this.apikey = apikey;
    this.authSchema = authSchema;
    this.checkIbanRestClient = checkIbanRestClient;
  }


  @Override
  public ResponseEntity<ResponseCheckIbanDTO> checkIban(String payOffInstr, String fiscalCode) {
    RequestCheckIbanDTO requestCheckIbanDTO=new RequestCheckIbanDTO();
    requestCheckIbanDTO.getAccount().setValue(payOffInstr);
    requestCheckIbanDTO.getAccount().setValueType(IBAN);
    requestCheckIbanDTO.getAccountHolder().setType(PERSON_NATURAL);
    requestCheckIbanDTO.getAccountHolder().setFiscalCode(fiscalCode);
    return checkIbanRestClient.checkIban(requestCheckIbanDTO, apikey, authSchema);
  }

}
