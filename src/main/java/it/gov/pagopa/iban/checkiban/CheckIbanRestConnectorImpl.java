package it.gov.pagopa.iban.checkiban;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.iban.dto.CheckIbanDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheckIbanRestConnectorImpl implements CheckIbanRestConnector {
  private final String apikey;
  private final String authSchema;
  private final CheckIbanRestClientMock checkIbanRestClientMock;
  private static final String IBAN="IBAN";
  private static final String PERSON_NATURAL="PERSON_NATURAL";

  public CheckIbanRestConnectorImpl(
      @Value("${rest-client.checkiban.apikey}") String apikey,
      @Value("${rest-client.checkiban.authSchema}") String authSchema,
      CheckIbanRestClientMock checkIbanRestClientMock) {
    this.apikey = apikey;
    this.authSchema = authSchema;
    this.checkIbanRestClientMock = checkIbanRestClientMock;
  }


  @Override
  public CheckIbanDTO checkIban(String payOffInstr, String fiscalCode) {
    String responseMock = checkIbanRestClientMock.checkIban();
    if(responseMock!=null ){
      return this.convertToJson(responseMock);
    }
    return null;
  }

  private CheckIbanDTO convertToJson(String string) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      CheckIbanDTO checkIbanDTO = mapper.readValue(string, CheckIbanDTO.class);
      return checkIbanDTO;
    }catch(Exception exception){
      exception.printStackTrace();
      return new CheckIbanDTO();
    }
  }

}
