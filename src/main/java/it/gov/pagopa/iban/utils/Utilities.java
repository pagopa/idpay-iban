package it.gov.pagopa.iban.utils;

import it.gov.pagopa.iban.exception.IbanException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class Utilities {
  private static final String SRCIP;

  static {
    try {
      SRCIP = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      throw new IbanException(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
  }

  private static final String CEF = String.format("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2|vs=172.16.151.21:80 event=Onboarding srcip=%s srcport=17548 dstip=172.16.128.37 dstport=82",
      SRCIP);
  private static final String MSG = " msg=";
  private static final String USER = "suser=";
  private static final String CS1 = "cs1Label=iniziativeId cs1=";
  final Logger logger = Logger.getLogger("AUDIT");


  private String buildLog(String eventLog, String userId, String initiativeId) {
    return CEF + MSG + eventLog + " " + USER + userId + " " + CS1 + initiativeId;
  }

  public void logCheckIbanOK(String userId, String initiativeId) {
    String testLog = this.buildLog("Checkiban's answer was OK ", userId,
        initiativeId);
    logger.info(testLog);
  }

  public void logCheckIbanUnknown(String userId, String initiativeId) {
    String testLog = this.buildLog("Checkiban's answer was Unknown ", userId,
        initiativeId);
    logger.info(testLog);
  }

  public void logCheckIbanKO(String userId, String initiativeId) {
    String testLog = this.buildLog("Checkiban's answer was KO ", userId,
        initiativeId);
    logger.info(testLog);
  }
}