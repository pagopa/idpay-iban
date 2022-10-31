package it.gov.pagopa.iban.constants;

public class IbanConstants {
  public static final String UNKNOWN_PSP="UNKNOWN_PSP";
  public static final String KO="KO";
  public static final String OK="OK";
  public static final String ERROR_MSG_HEADER_SRC_TYPE = "srcType";
  public static final String ERROR_MSG_HEADER_SRC_SERVER = "srcServer";
  public static final String ERROR_MSG_HEADER_SRC_TOPIC = "srcTopic";
  public static final String ERROR_MSG_HEADER_DESCRIPTION = "description";
  public static final String ERROR_MSG_HEADER_RETRYABLE = "retryable";
  public static final String ERROR_MSG_HEADER_STACKTRACE = "stacktrace";
  public static final String ERROR_MSG_HEADER_CLASS = "rootCauseClass";
  public static final String ERROR_MSG_HEADER_MESSAGE = "rootCauseMessage";
  public static final String TOPIC_IBAN = "idpay-checkiban-evaluation";
  public static final String KAFKA = "kafka";
  public static final String BROKER_IBAN = "cstar-d-idpay-evh-ns-00.servicebus.windows.net:9093";
  public static final String ERROR_IBAN = "error to ADD new IBAN";
  private IbanConstants(){

  }
}
