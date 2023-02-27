package it.gov.pagopa.iban.utils;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class AuditUtilitiesTest {
  private static final String USER_ID = "TEST_USER_ID";
  private static final String IBAN = "IBAN";
  private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
  private static final String REQUEST_ID = "TEST_REQUEST_ID";

  private final AuditUtilities auditUtilities = new AuditUtilities();
  private MemoryAppender memoryAppender;

  @BeforeEach
  public void setup() {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("AUDIT");
    memoryAppender = new MemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    logger.setLevel(ch.qos.logback.classic.Level.INFO);
    logger.addAppender(memoryAppender);
    memoryAppender.start();
  }

  private void checkCommonFields() {
    Assertions.assertTrue(memoryAppender.contains(ch.qos.logback.classic.Level.INFO,USER_ID));
    Assertions.assertTrue(memoryAppender.contains(ch.qos.logback.classic.Level.INFO,IBAN));
    Assertions.assertTrue(memoryAppender.contains(ch.qos.logback.classic.Level.INFO,INITIATIVE_ID));

    Assertions.assertEquals(1, memoryAppender.getLoggedEvents().size());
  }

  @Test
  void logCheckiban_ok(){
    auditUtilities.logCheckIbanOK(USER_ID,INITIATIVE_ID,IBAN, REQUEST_ID);
    checkCommonFields();

    Assertions.assertEquals(
            ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Iban dstip=%s msg=Checkiban's answer was OK suser=%s " +
                    "cs1Label=initiativeId cs1=%s cs2Label=iban cs2=%s cs3Label=checkIbanRequestId cs3=%s")
                    .formatted(
                            AuditUtilities.SRCIP,
                            USER_ID,
                            INITIATIVE_ID,
                            IBAN,
                            REQUEST_ID
                    ),
            memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
    );
  }

  @Test
  void logCheckiban_unknown(){
    auditUtilities.logCheckIbanUnknown(USER_ID,INITIATIVE_ID,IBAN, REQUEST_ID);
    checkCommonFields();

    Assertions.assertEquals(
            ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Iban dstip=%s msg=Checkiban's answer was UNKNOWN suser=%s " +
                    "cs1Label=initiativeId cs1=%s cs2Label=iban cs2=%s cs3Label=checkIbanRequestId cs3=%s")
                    .formatted(
                            AuditUtilities.SRCIP,
                            USER_ID,
                            INITIATIVE_ID,
                            IBAN,
                            REQUEST_ID
                    ),
            memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
    );
  }

  @Test
  void logCheckiban_ko(){
    auditUtilities.logCheckIbanKO(USER_ID,INITIATIVE_ID,IBAN, REQUEST_ID);
    checkCommonFields();

    Assertions.assertEquals(
            ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Iban dstip=%s msg=Checkiban's answer was KO suser=%s " +
                    "cs1Label=initiativeId cs1=%s cs2Label=iban cs2=%s cs3Label=checkIbanRequestId cs3=%s")
                    .formatted(
                            AuditUtilities.SRCIP,
                            USER_ID,
                            INITIATIVE_ID,
                            IBAN,
                            REQUEST_ID
                    ),
            memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
    );
  }
  @Test
  void logEnrollIban(){
    auditUtilities.logEnrollIban(USER_ID,INITIATIVE_ID,IBAN);
    checkCommonFields();

    Assertions.assertEquals(
            ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Iban dstip=%s msg=New IBAN enrolled from IO suser=%s " +
                    "cs1Label=initiativeId cs1=%s cs2Label=iban cs2=%s")
                    .formatted(
                            AuditUtilities.SRCIP,
                            USER_ID,
                            INITIATIVE_ID,
                            IBAN
                    ),
            memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
    );
  }
  @Test
  void logEnrollIbanFromIssuer(){
    auditUtilities.logEnrollIbanFromIssuer(USER_ID,INITIATIVE_ID,IBAN);
    checkCommonFields();

    Assertions.assertEquals(
            ("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Iban dstip=%s msg=New IBAN enrolled from Issuer suser=%s " +
                    "cs1Label=initiativeId cs1=%s cs2Label=iban cs2=%s")
                    .formatted(
                            AuditUtilities.SRCIP,
                            USER_ID,
                            INITIATIVE_ID,
                            IBAN
                    ),
            memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
    );
  }
}
