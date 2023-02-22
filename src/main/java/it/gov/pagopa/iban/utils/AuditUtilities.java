package it.gov.pagopa.iban.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j(topic = "AUDIT")
public class AuditUtilities {
  public static final String SRCIP;

  static {
    String srcIp;
    try {
      srcIp = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.error("Cannot determine the ip of the current host", e);
      srcIp="UNKNOWN";
    }
    SRCIP = srcIp;
  }

  private static final String CEF = String.format("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Iban dstip=%s", SRCIP);
  private static final String CEF_PATTERN = CEF + " msg={} suser={} cs1Label=initiativeId cs1={} cs2Label=iban cs2={}";
  private static final String CEF_CORRELATED_PATTERN = CEF_PATTERN + " cs3Label=checkIbanRequestId cs3={}";

  private void logAuditString(String pattern, String... parameters) {
    log.info(pattern, (Object[]) parameters);
  }

  public void logCheckIbanOK(String userId, String initiativeId, String iban, String requestId) {
    logAuditString(
            CEF_CORRELATED_PATTERN,
            "Checkiban's answer was OK", userId, initiativeId, iban, requestId
    );
  }

  public void logCheckIbanUnknown(String userId, String initiativeId, String iban, String requestId) {
    logAuditString(
            CEF_CORRELATED_PATTERN,
            "Checkiban's answer was UNKNOWN", userId, initiativeId, iban, requestId
    );
  }

  public void logCheckIbanKO(String userId, String initiativeId, String iban, String requestId) {
    logAuditString(
            CEF_CORRELATED_PATTERN,
            "Checkiban's answer was KO", userId, initiativeId, iban, requestId
    );
  }

  public void logEnrollIban(String userId, String initiativeId, String iban) {
    logAuditString(
            CEF_PATTERN,
            "New IBAN enrolled from IO", userId, initiativeId, iban
    );
  }
  public void logEnrollIbanFromIssuer(String userId, String initiativeId, String iban) {
    logAuditString(
            CEF_PATTERN,
            "New IBAN enrolled from Issuer", userId, initiativeId, iban
    );
  }
}